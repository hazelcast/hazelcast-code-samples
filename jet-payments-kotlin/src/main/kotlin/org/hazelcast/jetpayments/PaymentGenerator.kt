package org.hazelcast.jetpayments

/*
 * Class that generates a Kotlin Flow of simulated payments for us to process.
 */
class PaymentGenerator(
    private val seededRandom: kotlin.random.Random,
    private val merchantMap: Map<String, Merchant> = MerchantGenerator(
        AppConfig.numMerchants, seededRandom
    ).merchantMap,
) {
    fun newPaymentRequestSeq() = generateSequence(1) { it + 1 }.map { paymentId ->
        fun to2Digits(x: Double) = ((x * 100).toInt() / 100.0)
        val merchant = merchantMap.values.random(seededRandom)
        PaymentRequest(
            paymentId,
            to2Digits(paymentAmountNext()),
            Epoch.timeNow(), // will be updated to the issue time later
            merchant.id,
            merchant.name,
        )
    }
}
