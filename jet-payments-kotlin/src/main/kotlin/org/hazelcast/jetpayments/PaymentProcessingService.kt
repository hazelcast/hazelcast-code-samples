package org.hazelcast.jetpayments

import com.hazelcast.core.HazelcastInstance
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.future.future
import java.util.*
import kotlin.time.Duration.Companion.seconds

/*
 * This is a mockup of the payment processing service that would exist inside a
 * payments company. This service would represent the final leg to the payment
 * processor. There is a delay() function below which represents the API call to the
 * payment processor, and the time taken to wait on and process the response.
 */
internal class PaymentProcessingService(private val instance: HazelcastInstance) :
    AutoCloseable {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /*
     * All of our logging uses a member index (node number) to represent the nodes,
     * but this isn't something Hazelcast keeps track of; Hazelcast only identifies
     * members uniquely by their UUID. So if node 2 leaves, and another new member
     * joins in its place, we want to treat that new member as "node 2" for
     * simplicity, even though from Hazelcast's perspective, it's a completely new
     * member with a different UUID (than the previous node 2 had, or any other
     * member).
     *
     * Set this at init time, before we lose any nodes, so we get the right index
     * number. We track the indexes for each UUID in a global IMap tracked in
     * Hazelcast, and we'll look up the index here, waiting for the mapping to be
     * set.
     */
    private val memberIndexDeferred: Deferred<Int> = scope.async {
        val myUUID = instance.cluster.localMember.uuid
        val uuidToMemberIndexMap =
            instance.getMap<UUID, Int>(AppConfig.uuidToMemberIndexMapName)
        var index: Int? = null
        while (index == null) {
            delay(1.seconds) // Wait until mapping is made.
            index = uuidToMemberIndexMap[myUUID]
        }
        index
    }

    override fun close() {
        scope.cancel()
    }

    /*
     * Provide a facility for simulating payment processor failures, i.e. simulate
     * what would happen if the API call the Payment Processor failed somehow. This
     * was mostly for debugging purposes, but it might be interesting to turn this
     * on and see the result. Rather than doing random failures, we've done this
     * using a Kotlin StateFlow that simulates failures occuring regularly in
     * 'runs'. So for 2 report cycles (10s by default) we'll get a run of successful
     * payments, then for one report cycle we'll get failures, then we repeat.
     */
    private val paymentSucceeded = flow {
        while (AppConfig.simulatePaymentProcessorFailure) {
            delay(AppConfig.reportFrequency * 2) // work for 2/3 of time
            emit(false) // switches StateFlow to "fail payments"
            delay(AppConfig.reportFrequency) // fail 1/3 of time
            emit(true) // switches StateFlow back to "succeed payments"
        }
    }.stateIn(scope, SharingStarted.Eagerly, true)

    /*
     * This is the function that is actually called by the payment processing Jet
     * pipeline to process payments. It must be thread-safe. It simulates a call to
     * a real payment processor by doing (1) a random delay, and the creation of a
     * "payment receipt" that indicates whether the payment succeeded or not. The
     * downstream verification that all payments were paid checks both for the
     * presence of a payment receipt, and that the receipt shows the payment as
     * paid. We create a CompletableFuture here, rather than a Deferred, for
     * compatibility with Java code.
     */
    fun processPaymentAsync(paymentRequest: PaymentRequest) = scope.future {
        // Simulate the delay inherent in request-respond to payment processor
        delay(paymentProcessingDelayNext())
        PaymentReceipt(
            paymentRequest, paymentSucceeded.value, memberIndexDeferred.await()
        )
    }
}
