package org.hazelcast.jetpayments

import kotlinx.serialization.Serializable
import kotlin.time.Duration

/*
 * This Kotlin data class represents a payment *receipt*, which we create when the
 * payment processor finishes processing a payment. It links with the payment request
 * via the paymentId field. If the payment was successful, then this record will exist
 * *and* isPaId will be true. The timestamp from when the payment processor completed
 * the payment is marked in the timePaid field. The member on which the payment was
 * processed is recorded in onMember.
 */
@Serializable
data class PaymentReceipt(
    val paymentId: Int, // foreign key with the PaymentRequest.kt
    val merchantId: String,
    val merchantName: String,
    val isPaid: Boolean, // was this successfully paid by the processor?
    val timePaid: Duration, // when was this paid?
    val onMember: Int, // which node was this paid on?
) : java.io.Serializable, Comparable<PaymentReceipt> {
    // Secondary constructor
    constructor(
        paymentRequest: PaymentRequest,
        isPaid: Boolean,
        memberIndex: Int,
    ) : this(
        paymentRequest.paymentId,
        paymentRequest.merchantId,
        paymentRequest.merchantName,
        isPaid,
        Epoch.timeNow(),
        memberIndex
    )

    override fun compareTo(other: PaymentReceipt): Int =
        this.timePaid.compareTo(other.timePaid)
}
