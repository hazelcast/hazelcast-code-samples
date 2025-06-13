package org.hazelcast.jetpayments

import com.hazelcast.jet.pipeline.*
import kotlin.time.Duration.Companion.seconds

/*
 * A streaming Hazelcast jet job that reads from a streamsource (Kafka, in the
 * case of this demo) and verifies that every payment request from that source was
 * actually paid, by verifying with the payment receipt map. Jet does a hashJoin
 * between the stream (the requests) and the map (the receipts), and we look to
 * make sure that we have one for each and that the receipt shows that the payment
 * was successfully made.
 */
internal class PaymentProcessedCheckPipeline(
    client: HzCluster.ClientInstance,
    private val source: StreamSource<Map.Entry<Int, String>>,
    private val numPayments: Int,
) : StreamingJetPipeline(
    client, AppConfig.paymentProcessedCheckJetJobName.uniqify(), 0.seconds
) {
    private val paymentProcessedCheckList =
        client.getList<TimeRange>(AppConfig.paymentProcessedCheckListName)

    override fun describePipeline(): String {
        return """
            This Jet pipeline reads from the same Kafka stream source, but with a
            different Kafka consumer group so that it can rewind to the beginning
            and receive the same flow of payments. As before with Kafka, we first
            map the JSON representation stored in the topic to payment requests. Then
            we do a hash join of this stream with the receipt map, ensuring that
            every payment request resulted in a receipt, and it was paid. The
            results of this are graphed for the user's inspection. Successful
            payments are shown with a ✓, failed payments are shown with an ×. 
        """.trimIndent()
    }

    override val pipeline = Pipeline.create().apply {
        readFrom(source).withoutTimestamps()
            .map { entry -> entry.value.toPaymentRequest() }.hashJoin(
                readFrom(Sources.map<Int, PaymentReceipt?>(AppConfig.paymentReceiptMapName)),
                JoinClause.onKeys(
                    { request -> request.paymentId },
                    { receiptEntry -> receiptEntry?.value?.paymentId })
            ) { paymentRequest, (_, receipt) ->
                receipt?.let { receipt ->
                    TimeRange(
                        if (receipt.isPaid) "✓" else "×", receipt.timePaid
                    )
                } ?: TimeRange("×", paymentRequest.timeIssued)
            }.writeTo(Sinks.list(paymentProcessedCheckList))
    }

    // How many PaymentRequets remain unverified?
    override fun unitsLeft() =
        (numPayments - paymentProcessedCheckList.size).coerceAtLeast(0)
}
