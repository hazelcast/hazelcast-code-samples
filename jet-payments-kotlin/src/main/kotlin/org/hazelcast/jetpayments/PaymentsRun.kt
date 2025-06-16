package org.hazelcast.jetpayments

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

/*
 * This class represents the heart of the operation. It handles the execution of a
 * single payments run, of a certain number of payments. It orchestrates the
 * creation of resources, the execution of the payments run including the lifecycle
 * of the FailureSimulator and the ReceiptWatcher, the setup of Kafka and the
 * execution of the various Jet jobs, the verification of the results, and
 * ultimately the rendering of the results to ASCII-art via the Canvas class.
 */
class PaymentsRun() : AutoCloseable {
    private val logger = ElapsedTimeLogger("PaymentsRun")
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Cleanup for above
    override fun close() {
        scope.cancel()
    }

    // Start up Hazelcast asynchronously. Users of the client will call await().
    private val hzClientDeferred = scope.async {
        HzCluster("dev", AppConfig.clusterSize).getClient()
    }

    // Spin up Kafka
    private val kafkaTopicName = AppConfig.kafkaTopicName.uniqify()
    private val kafka = KafkaCluster<Int, String>(kafkaTopicName, 0)
    private val kafkaProcessCG = AppConfig.kafkaProcessPaymentsCG.uniqify()
    private val kafkaVerifyCG = AppConfig.kafkaVerifyPaymentsCG.uniqify()

    // Create the list of merchants we'll use throughout the payment run.
    private val merchantMap =
        MerchantGenerator(AppConfig.numMerchants, seededRandom).merchantMap

    // Create a PaymentGenerator that will synthesise new PaymentRequests for us.
    private val paymentGenerator = PaymentGenerator(
        seededRandom, merchantMap
    ) { nextPaymentRequestDelay() }

    private fun calculateNumPayments(numFailureCycles: Int): Int {
        val failureTime = AppConfig.failureCycleTime * numFailureCycles
        val totalTime = AppConfig.warmupTime + failureTime + AppConfig.cooldownTime
        return (totalTime / AppConfig.paymentRequestDelayRand.mean.milliseconds).toInt()
    }

    // Display our key parameters at the start of this payments run.
    private suspend fun showPaymentRunParameters(
        numFailureCycles: Int, numPayments: Int
    ) {
        // allow time for the receipts to catch up at the end and be processed
        val meanIssueDelay = AppConfig.paymentRequestDelayRand.mean.milliseconds
        val paymentIssuePeriod = meanIssueDelay * numPayments
        val clusterSize = hzClientDeferred.await().originalClusterSize
        val clusterMembers = (0 until clusterSize).joinToString { fontEtched(it) }
        val longestName = merchantMap.values.maxOf { it.name.length }

        val left = TextBox(buildList {
            add("Hazelcast cluster nodes -> $clusterMembers")
            add("Payment issue -> $numPayments payments in ~$paymentIssuePeriod")
            mapOf(
                "request" to AppConfig.paymentRequestDelayRand,
                "processing" to AppConfig.paymentProcessingDelayRand,
            ).map { (prefix, rand) ->
                "$prefix delay -> µ=${rand.mean.milliseconds} σ=${rand.stddev.milliseconds}"
            }.also { addAll(it) }
        }).center()

        val middle = TextBox(
            listOf(
                italic("FAILURE SIMULATOR ACTIONS"), ""
            ) + buildList {
                add(AppConfig.warmupTime.toWholeSecStr())
                addAll(List(numFailureCycles) {
                    "[${(AppConfig.failureCycleTime)} ↓↑]"
                })
                add(AppConfig.cooldownTime.toWholeSecStr())
            }.joinToString(" -> ")
        ).center().addBorder()

        val right = TextBox(
            listOf(
                italic("OUR MERCHANTS"), ""
            ) + merchantMap.values.map { merchant ->
                with(merchant) {
                    "${name.padEnd(longestName)} '${shortName}': $id"
                }
            }).center().addBorder()

        left.stack(middle, addSpacer = false).adjoin(right)
            .addBorder(TextBox.BorderStyle.THICK).log(logger)
    }

    // Take the payments from our payments generator, and publish them to Kafka.
    private suspend fun issuePaymentsToKafka(
        numPayments: Int, countNewPayment: () -> Unit
    ) {

        /* Create a flow of numPayments randomly-generated payments with
         * inbuilt delays simulating the delays with real payments coming in.
         */
        paymentGenerator.newPaymentRequestFlow().take(numPayments).map { paymentReq ->
            Json.Default.encodeToString<PaymentRequest>(paymentReq)
        }.collect { jsonPaymentRequest ->
            kafka.publish(jsonPaymentRequest)
            countNewPayment() // Count new payment sent to Kafka
        }
    }

    // Show that everything got paid correctly (and some stats).
    private suspend fun verifyPayments(
        numPayments: Int, uptimeSpans: List<TimeSpan>
    ) {
        // Validate that every payment was processed correctly.
        val paid = showAllPaymentRequestsPaid(numPayments)
        // Show that only one node processed a given merchant at one time
        val byMerchant = showPaymentsDistributedByMerchant()
        // Draw all the timespans
        Canvas(paid + byMerchant + uptimeSpans).draw().log(logger)
    }

    private fun validationCheck(
        test: Boolean, succeedMsg: String, failedMsg: String
    ) = (if (test) "CHECK SUCCEEDED: $succeedMsg"
    else "CHECK FAILED: $failedMsg").let {
        TextBox(
            listOf(bold(it)), borderStyle = TextBox.BorderStyle.SINGLE
        ).log(logger)
    }

    /* Show that each and every payment request resulted in a receipt, meaning that
     * it was paid by payment processing service.
     */
    private suspend fun showAllPaymentRequestsPaid(numPayments: Int): List<TimeSpan> {

        /* Step I: Run a streaming Jet job to replay everything from Kafka, and
         * check we have a receipt. For each receipt, put a TimeRange into our
         * IList.
         */
        PaymentProcessedCheckPipeline(
            hzClientDeferred.await(),
            kafka.consumerJetSource(kafkaVerifyCG),
            numPayments
        ).use { it.runUntilCompleted() }

        /* Step II: Sort and fold the contiguous TimeRanges, coalescing into longer
         * ranges except where they differ in terms of payment success. Partition
         * into "succeeded" and "failed" lists. If things worked as planned, we'll
         * only have the former, with a single TimeRange inside.
         */
        val paymentProcessedCheckList = hzClientDeferred.await()
            .getList<TimeRange>(AppConfig.paymentProcessedCheckListName)
        val timeRanges = foldTimeRanges(paymentProcessedCheckList.sorted())
        paymentProcessedCheckList.clear() // clear for next run
        val (succeeded, failed) = timeRanges.partition { it.marker == "✓" }
        if (!AppConfig.simulatePaymentProcessorFailure) {
            validationCheck(
                succeeded.size == 1 && failed.isEmpty(),
                "All payments were processed by the payment processor.",
                "Some payments were failed by the payment processor, got success=$succeeded, failed=$failed"
            )
        }

        /* Step III: Convert our successful and failed TimeRange groups into
         * TimeSpans, for later conversion into a Canvas for display.
         */
        return buildList {
            add("✓ PAID" to succeeded)
            add("× FAIL" to failed)
        }.filter { it.second.isNotEmpty() }.map { (prefix, timeRanges) ->
            TimeSpan(prefix, timeRanges)
        }
    }

    /* Show that each merchant was always assigned a particular node any given moment--and that
     * in failure/recovery situations, that node would automatically be reassigned.
     */
    private suspend fun showPaymentsDistributedByMerchant(): List<TimeSpan> {

        /* Step I: Run a batch Jet job to show, for each merchant, on which
         * members their payments were paid at any given moment. The resulting
         * map is keyed off merchantId, and the values are lists of 0-width
         * TimeRanges with the marker being the node on which the payment was
         * processed.
         */
        PaymentMemberCheckPipeline(hzClientDeferred.await()).use { it.run() }

        /* Step II: Collect the TimeRanges (payment receipts) for each merchant
         * back from the jet job and fold the contiguous ones together,
         * separating by the node selected for processing. For each merchant, we
         * should see that none of the TimeRanges should overlap, because that
         * would indicate a case where the same merchant was being serviced from
         * two different nodes. */
        val paymentOnOneNodeCheckMap = hzClientDeferred.await()
            .getMap<String, List<TimeRange>>(AppConfig.paymentOnOneNodeCheckMapName)
        val timeRangesByMerchant =
            paymentOnOneNodeCheckMap.mapValues { (_, timeRanges) ->
                foldTimeRanges(timeRanges)
            }
        paymentOnOneNodeCheckMap.clear() // clear for next run
        validationCheck(
            !timeRangesByMerchant.values.any { doRangesOverlap(it) },
            "Each merchant's payments were processed on one node at a time.",
            "Some merchants' payments were paid on two or more nodes at once."
        )

        /* Step III: Convert the list of TimeRanges for each merchant to a TimeSpan,
         * and return them so they can be represented on a Canvas.
         */
        return timeRangesByMerchant.map { (merchantName, timeRanges) ->
            val prefix = merchantMap[merchantName]!!.name
            TimeSpan(prefix, timeRanges.map { range ->
                TimeRange(
                    fontEtched(range.marker.toInt()), range.start, range.endInclusive
                )
            })
        }.sorted() // sort TimeSpans by start time
    }

    /*
     * PaymentsRun entry point. Do the payments run, and verify afterwards. This
     * is a _suspend_ method, meaning it must be called from a coroutine, and
     * that coroutine might suspend at some point. Coroutines, if you're not
     * familiar with them, are a way of writing asynchronous code that's easier
     * to understand, and that aren't as heavyweight as threads. They require
     * some degree of cooperation and planning to work, but Kotlin provides
     * language support for understanding and managing coroutine concurrency.
     */
    internal suspend fun run(numFailureCycles: Int) = withContext(Dispatchers.Default) {
        val client = hzClientDeferred.await()
        val numPayments = calculateNumPayments(numFailureCycles)
        showPaymentRunParameters(numFailureCycles, numPayments)

        // Create a counter for the number of payments we've issued.
        MutableStateFlow(0).let { numIssued ->
            // First start the payments flow into Kafka, from which Jet will read
            launch { issuePaymentsToKafka(numPayments) { numIssued.value++ } }
            // A service that monitors completed receipts, summarizes and logs them.
            ReceiptWatcher(client, numPayments, merchantMap) { numIssued.value }
        }.use { watcher ->

            // A service that simulates failures of individual nodes in the cluster.
            val failSim = FailureSimulator(client) { watcher.nodesInUse.value }

            // Read from Kafka, distribute to members, process payments
            PaymentsJetPipeline(
                client, kafka.consumerJetSource(kafkaProcessCG), numPayments
            ).use { pipeline ->
                launch { pipeline.run() } // Start Jet streaming pipeline.

                coroutineScope {
                    launch { watcher.startReceiptsLog() }
                    async {
                        failSim.runSimulations(
                            pipeline.jetSchedulerStateFlow, pipeline::hasTimeLeft
                        )
                    }
                }.await()
            } // We hit close() on pipeline, which stops the Jet job.
        }.also { uptimeSpans -> verifyPayments(numPayments, uptimeSpans) }
    }
}
