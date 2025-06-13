import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.zip
import org.hazelcast.jetpayments.*
import kotlin.math.absoluteValue
import kotlin.test.Test
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.seconds

class PaymentsTest {
    private val logger = ElapsedTimeLogger("PaymentsTest")
    private val context = Dispatchers.Default + SupervisorJob()

    @Test
    fun `test runPaymentsFlow with different test periods`() =
        runBlocking(context) {
            val numTests = 4
            val testRuns = List(numTests) { 1 }
            //val testRuns = generateSequence(1) { it + 1 }.take(numTests).toList()
            val numPaymentsArray = testRuns.map { numFailureCycles ->
                "$numFailureCycles failure cycles"
            }

            fun showNthTest(testNum: Int) = TextBox(buildList {
                numPaymentsArray.forEachIndexed { index, s ->
                    val testStr = "TEST $index: $s"
                    val line = if (index == testNum) "run-> $testStr <-now"
                    else testStr
                    add(line)
                }
            }).center().addBorder().log(logger)

            PaymentsRun().use { paymentsRun ->
                testRuns.forEachIndexed { testNum, numFailureCycles ->
                    Epoch.reset()
                    showNthTest(testNum)
                    paymentsRun.run(numFailureCycles)
                }
            }
        }

    @Test
    fun `test KafkaCluster to ensure re-readability`() = runBlocking(context) {
        val numPayments = 2000
        val paymentGenerator = PaymentGenerator(seededRandom)
        val consumerGroup = AppConfig.kafkaVerifyPaymentsCG.uniqify()
        val channel = Channel<PaymentRequest>(numPayments)
        val channelFlow = channel.consumeAsFlow()

        KafkaCluster<Int, String>(
            AppConfig.kafkaTopicName.uniqify(), 0
        ).use { kafka ->
            withContext(Dispatchers.IO) {
                val kafkaFlow = kafka.consumeAsFlow(
                    consumerGroup, numPayments
                ) { it.toPaymentRequest() }

                coroutineScope {
                    launch {
                        var numChecked = 0
                        channelFlow.zip(kafkaFlow) { checkPmtReq, pmtReq ->
                            pmtReq to checkPmtReq
                        }.collect { (pmtReq, checkPmtReq) ->
                            check(pmtReq == checkPmtReq) { "Payment mismatch $pmtReq != $checkPmtReq" }
                            numChecked++
                            if (numChecked % 100 == 0) logger.log("Checked $numChecked/$numPayments")
                        }
                    }

                    launch {
                        var numPublished = 0
                        paymentGenerator.newPaymentRequestSeq().take(numPayments)
                            .forEach { pmtreq ->
                                kafka.publish(pmtreq.toJsonString())
                                channel.send(pmtreq)
                                numPublished++
                                if (numPublished % 100 == 0) logger.log("Published $numPublished/$numPayments")
                            }
                        channel.close() // Close send channel here so receiver will terminate
                    }
                }
            }
        }

        logger.log("Validated all payments")
    }

    @Test
    fun `test creation of Canvas Timescales`() = runBlocking(context) {
        val canvasSizeSeq = generateSequence(54) { it + 6 }.takeWhile { it <= 196 }
        val merchantMap =
            MerchantGenerator(AppConfig.numMerchants, seededRandom).merchantMap
        canvasSizeSeq.forEach { canvasSize ->
            val minTime = 0.seconds
            val totalTimeSeq =
                generateSequence(canvasSize.seconds) { it + canvasSize.seconds / 4 }.takeWhile { it <= canvasSize.seconds * 2 }
            totalTimeSeq.forEach { totalTime ->
                val maxTime = minTime + totalTime

                val timespans = merchantMap.values.map { merchant ->
                    val numRanges = canvasSize / 16
                    (1..numRanges * 2).map {
                        seededRandom.nextLong(
                            minTime.inWholeMicroseconds, maxTime.inWholeMicroseconds
                        ).microseconds
                    }.sorted().chunked(2).map { (start, endInclusive) ->
                        TimeRange(
                            merchant.name.first().toString(), start, endInclusive
                        )
                    }.let { timeRanges ->
                        TimeSpan(merchant.name, timeRanges)
                    }
                }.sorted()

                val canvas = Canvas(timespans, canvasSize)
                val lines = canvas.draw()
                assert(lines.map { it.length }.all { it == lines.first().length })
                TextBox(buildList {
                    add("Canvas size: ${canvas.canvasSize}")
                    add("totalCanvasTime: ${canvas.totalCanvasTime}")
                }).justify().addBorder().log(logger)
                lines.log(logger)
            }
        }
    }

    @Test
    fun `test merchant distribution verification logic`() = runBlocking(context) {
        val numPayments = 100
        val clusterSize = AppConfig.clusterSize
        val runLength = numPayments / clusterSize / 2

        val rotationSeq = sequence {
            while (true) {
                repeat(clusterSize) { i ->
                    repeat(runLength) { yield(i) }
                }
            }
        }

        val client = HzCluster("dev", AppConfig.clusterSize).getClient()
        val paymentReceiptMap =
            client.getMap<Int, PaymentReceipt>(AppConfig.paymentReceiptMapName)
        val paymentMemberDistributionCheckMap =
            client.getMap<String, List<TimeRange>>(AppConfig.paymentOnOneNodeCheckMapName)
        val channel = Channel<PaymentRequest>(numPayments)

        logger.log("Populating receipt map")
        (PaymentGenerator(seededRandom).newPaymentRequestSeq() zip rotationSeq).take(
            numPayments
        ).forEach { (payreq, rotate) ->
            val paidOnMember =
                (payreq.merchantId.hashCode() + rotate).absoluteValue % clusterSize
            val receipt = PaymentReceipt(payreq, true, paidOnMember)
            paymentReceiptMap.put(receipt.paymentId, receipt)
            channel.send(payreq)
        }
        channel.close() // Close send channel here receiver will terminate

        /*
         * Now run our Jet job to process down the payment receipts.
         */
        logger.log("Running PaymentMemberCheckPipeline")
        PaymentMemberCheckPipeline(client).use { it.run() }

        val timeRangeIteratorByMerchant =
            paymentMemberDistributionCheckMap.mapValues { (_, timeRanges) ->
                timeRanges.iterator()
            }

        logger.log("Checking that all timeRanges match")
        var numChecked = 0
        for (payreq in channel) {
            if (numChecked % 10 == 0) logger.log("Checked #$numChecked")
            val timeRange = timeRangeIteratorByMerchant[payreq.merchantId]!!.next()
            val receipt = paymentReceiptMap[payreq.paymentId]!!
            assert(fontEtched(receipt.onMember) == timeRange.marker) { "paidOnMember mismatch ${receipt.onMember} != ${timeRange.marker} for $payreq" }
            assert(timeRange.start == timeRange.endInclusive) { "start/endInclusive mismatch $timeRange for $payreq" }
            assert(timeRange.start == receipt.timePaid) { "timePaid mismatch $timeRange for $receipt" }
            numChecked++
        }

        logger.log("Validated all payments")
    }

    @Test
    fun `test TallyField generate function`() = runBlocking(context) {
        val merchant = MerchantGenerator(
            1, seededRandom
        ).merchantMap.values.first().shortName
        val tallySeq = generateSequence(0) { i -> i + 1 }.map { i ->
            ReceiptWatcher.NodeTally(11 + i * 10, i)
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
                ReceiptWatcher.TallyField(fieldWidth, merchant, tally).generate()
            }
            logger.log(generateLogLine(fields))
        }
    }
}
