package org.hazelcast.jetpayments

import com.hazelcast.jet.pipeline.Pipeline
import com.hazelcast.jet.pipeline.ServiceFactories.sharedService
import com.hazelcast.jet.pipeline.Sinks
import com.hazelcast.jet.pipeline.StreamSource
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

/*
 * Streaming jet job that consumes PaymentRequests from Kafka, and groups them
 * across the cluster by merchantId, hands them to the PaymentProcessingService,
 * gets the PaymentReceipt back and stores it in the payment receipt map. It
 * overrides the unitsLeftToProcess method to show how many payments remain
 * unprocessed.
 */
class PaymentsJetPipeline(
    client: HzCluster.ClientInstance,
    private val streamSource: StreamSource<Map.Entry<Int, String>>,
    private val numPayments: Int,
) : StreamingJetPipeline(
    client,
    AppConfig.paymentProcessingJetJobName.uniqify(),
    AppConfig.paymentRequestDelayRand.mean.milliseconds
) {
    private val paymentReceiptMap =
        client.getMap<Int, PaymentReceipt>(AppConfig.paymentReceiptMapName).also {
            it.clear() // clear out any existing entries in case this is a restart
        }

    override fun describePipeline(): String {
        return """
           This Jet pipeline consumes payment requests in JSON format from
           Kafka, maps them from JSON to a payment request object, and uses a
           groupingKey to distribute the payment requests across the nodes
           according to merchant ID. The node assigned for the given merchant
           then uses its local payment processing service to process the
           merchant's payments. That service completes the payment after a
           random delay (simulating a real payment processing service), and
           returns a payment receipt. The receipt is then stored in the 
           payment receipt map.
           """.trimIndent()
    }

    override val pipeline: Pipeline = Pipeline.create().apply {
        readFrom(streamSource).withoutTimestamps().map { entry ->
            Json.Default.decodeFromString<PaymentRequest>(entry.value)
        }.groupingKey { it.merchantId } /* distribute/group by merchant ID */
            .mapUsingServiceAsync(
                sharedService { ctx ->
                    PaymentProcessingService(ctx.hazelcastInstance())
                }) { service, _, pmt ->
                service.processPaymentAsync(pmt) // pay it!
            }.writeTo(Sinks.map(paymentReceiptMap, { it.paymentId }, { it }))
    }

    override fun unitsLeft(): Int =
        (numPayments - paymentReceiptMap.size).coerceAtLeast(0)
}
