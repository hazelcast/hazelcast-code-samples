package org.hazelcast.jetpayments

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration

/*
 * Kotlin data class that represents the request to make a payment. These are mocked
 * up in Kafka, then consumed from Kafka by our PaymentsJetPipeline. We use these
 * for working with PaymentRequests in the Jet pipeline, but they are not recorded
 * into any maps. Our PaymentProcessedCheckPipeline Jet job will actually re-read
 * the same PaymentRequests from Kafka a 2nd time.
 */
@Serializable
data class PaymentRequest(
    val paymentId: Int, // foreign key with PaymentReceipt
    val amount: Double,
    val timeIssued: Duration, // when the payment was published to Kafka
    val merchantId: String,
    val merchantName: String,
) : java.io.Serializable

fun String.toPaymentRequest() = Json.Default.decodeFromString<PaymentRequest>(this)
fun PaymentRequest.toJsonString(): String = Json.Default.encodeToString(this)
