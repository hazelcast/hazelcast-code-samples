package org.hazelcast.jetpayments

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onEach
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/*
 * Class that generates a Kotlin Flow of simulated payments for us to process.
 */
class PaymentGenerator(
    private val seededRandom: Random,
    private val merchantMap: Map<String, Merchant> = MerchantGenerator(
        AppConfig.numMerchants, seededRandom
    ).merchantMap,
    private val nextDelay: () -> Duration = { 0.seconds },
) {
    fun newPaymentRequestFlow(): Flow<PaymentRequest> =
        generateSequence(1) { it + 1 }.map { paymentId ->
            fun to2Digits(x: Double) = ((x * 100).toInt() / 100.0)
            val merchant = merchantMap.values.random(seededRandom)
            PaymentRequest(
                paymentId,
                to2Digits(nextPaymentAmount()),
                Epoch.timeNow(),
                merchant.id,
                merchant.name,
            )
        }.asFlow().onEach { delay(nextDelay()) }
}
