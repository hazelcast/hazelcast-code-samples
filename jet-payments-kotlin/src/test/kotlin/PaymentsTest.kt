import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import org.hazelcast.jetpayments.*
import org.hazelcast.jetpayments.PaymentRequest
import java.util.*
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentsTest : FunSpec({
    val logger = ElapsedTimeLogger("PaymentsTest")
    val context = Dispatchers.Default + SupervisorJob()
    val merchantMap =
        MerchantGenerator(AppConfig.numMerchants, seededRandom).merchantMap
    val longestName = merchantMap.values.maxOf { it.name.length }

    fun logMerchants() {
        TextBox(listOf("MERCHANTS:") + merchantMap.values.map { merchant ->
            with(merchant) {
                "${name.padEnd(longestName)} '${shortName}': $id"
            }
        }).center().addBorder().log(logger)
    }

    val clusterSize = AppConfig.clusterSize
    val paymentFlow =
        PaymentGenerator(seededRandom, merchantMap).newPaymentRequestFlow()
    val numPayments = 2_000

    fun getPaymentFlow(runLength: Int = numPayments / clusterSize / 2): Flow<Pair<PaymentRequest, PaymentReceipt>> {
        val rotationFlow = flow {
            while (true) {
                for (node in 0 until clusterSize) {
                    repeat(runLength) { emit(node) }
                }
            }
        }
        return paymentFlow.zip(rotationFlow) { payreq, rotate ->
            val paidOnMember =
                (payreq.merchantId.hashCode().absoluteValue + rotate) % clusterSize
            payreq to PaymentReceipt(
                payreq, true, paidOnMember
            )
        }
    }

    val client = runBlocking {
        HzCluster("dev", AppConfig.clusterSize).getClient()
    }
    val paymentReceiptMap =
        client.getMap<Int, PaymentReceipt>(AppConfig.paymentReceiptMapName)

    test("test KafkaCluster to ensure re-readability") {
        val consumerGroup = AppConfig.kafkaVerifyPaymentsCG.uniqify()

        KafkaCluster<Int, String>(
            AppConfig.kafkaTopicName.uniqify(), 0
        ).use { kafka ->
            val checkFlow = produce(
                context = Dispatchers.IO, capacity = Channel.UNLIMITED
            ) {
                PaymentGenerator(seededRandom).newPaymentRequestFlow().take(numPayments)
                    .onEach { pmtreq ->
                        send(pmtreq)
                    }.map { pmtreq ->
                        Json.Default.encodeToString<PaymentRequest>(pmtreq)
                    }.collect { jsonPaymentRequest ->
                        kafka.publish(jsonPaymentRequest)
                    }
            }.consumeAsFlow()

            kafka.consume(consumerGroup) { serializedPayment ->
                Json.Default.decodeFromString<PaymentRequest>(serializedPayment)
            }.take(numPayments).zip(checkFlow) { pmtReq, checkPmtReq ->
                pmtReq to checkPmtReq
            }.filter { (pmtReq, checkPmtReq) -> pmtReq != checkPmtReq }
                .collect { (pmtReq, checkPmtReq) ->
                    fail("Payment mismatch: $pmtReq $checkPmtReq")
                }
        }
    }

    test("test creation of Canvas Timescales") {
        val canvasSizeSeq = generateSequence(54) { it + 30 }.takeWhile { it <= 144 }

        /*
         * Outer loop: Try out different canvas sizes, from
         * smaller to larger, up to the maximum of 196...
         */
        canvasSizeSeq.forEach { canvasSize ->
            /*
             * Inner loop: ...for each canvas size, loop through a set of
             * timescales that we can easily reasonably fit to that canvas
             * size. For each time scale, generate a Canvas and render it.
             */
            generateSequence(1.0) { it + 0.5 }.takeWhile { it <= 2.0 }
                .forEach { scaleFactor ->
                    val totalTime = (canvasSize * scaleFactor).seconds
                    val unit = DurationUnit.MILLISECONDS
                    val maxTime = totalTime
                    val maxRanges = canvasSize / 16
                    fun randomTime() =
                        seededRandom.nextLong(maxTime.toLong(unit)).toDuration(unit)

                    buildList {
                        add("canvasSize: $canvasSize")
                        add("maxRanges: $maxRanges")
                        add("maxTime: ${maxTime.toWholeSecStr()}")
                    }.let { lines ->
                        TextBox(lines).addBorder().log(logger)
                    }

                    /*
                     * Generate a TimeSpan (set of TimeRanges) for each Merchant.
                     * These will be used collectively to create a Canvas.
                     */
                    val timeSpans = merchantMap.values.map { merchant ->
                        val marker = merchant.name.first().toString()
                        // Choose a random number of ranges.
                        val numRanges = seededRandom.nextInt(2, maxRanges + 1)

                        /*
                         * For each range, we need two numbers--the boundaries of the
                         * range. Generate numRanges * 2 random numbers, sort them,
                         * and we'll select the ranges pairwise from the sorted list.
                         */
                        val boundaries = List(numRanges * 2) { randomTime() }

                        val timeRanges = boundaries.sorted().chunked(2)
                            .map { (start, endInclusive) ->
                                TimeRange(marker, start, endInclusive)
                            }

                        val prefix = "${merchant.name}: ${timeRanges.size} ranges"
                        TimeSpan(prefix, timeRanges)
                    }

                    val canvas = Canvas(timeSpans.sorted(), canvasSize)
                    val canvasLines = canvas.draw()

                    /*
                     * Render the Canvas, and check that all lines are the
                     * same length.
                     */
                    assert(canvasLines.map { it.length }
                        .all { it == canvasLines.first().length }) { "Canvas row lengths differ" }
                    canvasLines.log(logger)
                }
        }
    }

    test("test merchant distribution verification logic") {
        logMerchants()
        val numWritten = MutableStateFlow(0)
        paymentReceiptMap.clear()

        val requestChannel = produce(
            context = Dispatchers.IO, capacity = Channel.UNLIMITED
        ) {
            getPaymentFlow().take(numPayments).collect { (payreq, receipt) ->
                paymentReceiptMap.put(receipt.paymentId, receipt)
                send(payreq)
                numWritten.value++
            }
        }

        while (numWritten.value < numPayments) {
            delay(0.5.seconds)
        }

        /*
         * Now run our Jet job to process down the payment receipts.
         */
        PaymentMemberCheckPipeline(client).use { it.run() }

        val timeRangeIteratorByMerchant =
            client.getMap<String, List<TimeRange>>(AppConfig.paymentOnOneNodeCheckMapName)
                .mapValues { (_, timeRanges) ->
                    assert(timeRanges.isNotEmpty()) { "timeRanges is empty" }
                    assert(timeRanges.isSorted()) { "timeRanges is not sorted" }
                    timeRanges.iterator()
                }

        /*
         * Use the channel we set up above to go through the payment requests again,
         * verifying that the TimeRange values match the payment receipt.
         */
        for (payreq in requestChannel) {

            /*
             * Look up the TimeRange iterator for the merchant
             */
            assert(timeRangeIteratorByMerchant.containsKey(payreq.merchantId)) { "merchantId ${payreq.merchantId} not found in map" }
            val iter = timeRangeIteratorByMerchant[payreq.merchantId]!!
            assert(iter.hasNext()) { "no more time ranges for merchant ${payreq.merchantId}" }
            val timeRange = iter.next()

            // Check the marker value
            val receipt = paymentReceiptMap[payreq.paymentId]!!
            assert(receipt.onMember.toString() == timeRange.marker) {
                "paidOnMember mismatch ${receipt.onMember} != ${timeRange.marker} for $payreq"
            }

            // Should be a 0-width timestamp, using the receipt timePaid timestamp
            assert(timeRange.start == timeRange.endInclusive) { "start/endInclusive mismatch $timeRange for $payreq" }
            assert(timeRange.start == receipt.timePaid) { "timePaid mismatch $timeRange for $receipt" }
        }
    }

    test("test TallyField generate function") {
        val merchant = merchantMap.values.first().shortName
        val tallySeq = generateSequence(0) { i -> i + 1 }.map { i ->
            ReceiptSummary.NodeTally(11 + i * 10, i)
        }
        val metricsSummary = "[9999 QUD ⇒ 9999 WKG ⇒ 9999 FIN]➣"
        fun generateLogLine(fields: Iterable<String>): String =
            "$metricsSummary⟦ ${fields.joinToString(" | ")} ⟧"

        val numFields = AppConfig.numMerchants
        val prefixLen = 20
        val emptyFieldLine = generateLogLine(List(numFields) { "" })
        val fieldWidth =
            (AppConfig.screenWidth - emptyFieldLine.numCodepoints() - prefixLen) / numFields
        TextBox(
            listOf("width=$fieldWidth"), borderStyle = TextBox.BorderStyle.DOUBLE
        ).log(logger)
        for (numNodes in 1..9) {
            val tally = tallySeq.take(numNodes).toList()
            val fields = List(numFields) {
                TallyField(fieldWidth, merchant, tally).generate()
            }
            logger.log(generateLogLine(fields))
        }
    }

    test("test ReceiptWatcher") {
        val speedup = 4
        val numPayments = 500
        paymentReceiptMap.clear()
        logMerchants()
        val numWritten = MutableStateFlow(0)

        ReceiptWatcher(
            client, numPayments, merchantMap, AppConfig.reportFrequency / speedup
        ) { numWritten.value }.use { watcher ->
            withContext(context) {
                launch {
                    getPaymentFlow().take(numPayments).map { it.second }
                        .collect { receipt ->
                            numWritten.value++
                            paymentReceiptMap.put(receipt.paymentId, receipt)
                            delay(nextPaymentRequestDelay() / speedup)
                        }
                }

                watcher.startReceiptsLog()
            }
        }

        assert(paymentReceiptMap.size == numPayments) { "paymentReceiptMap.size=${paymentReceiptMap.size} != $numPayments" }
    }

    test("test ReceiptSummary") {
        logMerchants()
        val receiptSummary = ReceiptSummary()
        paymentReceiptMap.clear()

        getPaymentFlow().take(numPayments).map { it.second }.collect { receipt ->
            paymentReceiptMap.put(receipt.paymentId, receipt)
            receiptSummary.addReceipt(receipt)
        }

        data class ReceiptSummaryCapture(
            val numReceipts: Int,
            val nodesInUse: Set<Int>,
            val tallyMap: SortedMap<String, List<ReceiptSummary.NodeTally>>
        ) {
            override fun toString(): String {
                return buildList {
                    add("numReceipts=$numReceipts")
                    add("nodesInUse=$nodesInUse")
                    tallyMap.forEach { (merchant, tallyList) ->
                        val field = TallyField(
                            AppConfig.displayWidth, merchant, tallyList
                        ).generate().trim()
                        val total = tallyList.sumOf { it.numProcessed }
                        add("$field: TOTAL=$total")
                    }
                }.joinToString("\n")
            }
        }

        fun ReceiptSummary.capture(): ReceiptSummaryCapture = ReceiptSummaryCapture(
            numReceipts,
            nodesInUse,
            merchantMap.mapValues { (_, merchant) ->
                getTallyForMerchant(merchant.id)
            }.toSortedMap(),
        )

        val before = receiptSummary.capture()
        receiptSummary.rebuildFromMap(paymentReceiptMap)
        val after = receiptSummary.capture()
        assert(before == after) { "before != after; before:\n$before\nafter:\n$after" }
    }
})
