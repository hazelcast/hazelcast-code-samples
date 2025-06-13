package org.hazelcast.jetpayments

import com.hazelcast.core.EntryEvent
import com.hazelcast.map.listener.EntryAddedListener
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/*
 * This class observes the creation of PaymentReceipts, and summarizes these and
 * periodically logs that summary to the log. Additionally, since it's already
 * tracking which nodes are processing the payments, it also keeps track of those
 * for the FailureSimulator so it knows which knows it should bring down and up for
 * maximum visible impact.
 *
 * For each merchant the summary shows the nodes on which the merchant's payment
 * requests were processed, in order. So if, for example, the FailureSimulator
 * brings down node 1, and merchant X's payments were being processed on node 1 but
 * now must be rescheduled to node 2, the ReceiptWatcher will track that as "1->2",
 * showing that the payments were all being done on node 1, but now are being done
 * on node 2.
 *
 * This class creates a Hazelcast entry listener on the payment receipt map, that
 * continuously reports on new insertions to that map. These are transmitted to a
 * coroutine managed by this class, which puts them into a summary map of payment
 * receipts, organized in a way that is optimal for generating the summary shown
 * above. The receipts are summarized by merchant, and for each merchant, a list of
 * nodes that receipts were processed on, in order. We keep track of the nodes used
 * to process receipts, and the count processed on each node, in time order. This
 * makes it easy to generate the summary.
 */
class ReceiptWatcher(
    client: HzCluster.ClientInstance,
    private val numPayments: Int,
    private val merchantMap: Map<String, Merchant>,
    private val getNumPaymentsReceived: () -> Int,
) : AutoCloseable {
    private val logger = ElapsedTimeLogger("Watcher")
    private val paymentReceiptMap =
        client.getMap<Int, PaymentReceipt>(AppConfig.paymentReceiptMapName)
    private val screenWidth: Int = AppConfig.screenWidth

    // Data class that tracks the number of receipts processed on a particular node.
    data class NodeTally(var numProcessed: Int, val onMember: Int)

    // Public state that reveals which nodes are currently processing receipts.
    val nodesInUse = MutableStateFlow<List<Int>>(emptyList())

    /*
     * Data structures needed to communicate with the ReceiptsActor, below.
     */
    data class Summary(val numReceipts: Int, val receiptSummary: String)
    sealed class ActorMessage {
        class Update(val receipt: PaymentReceipt) : ActorMessage()
        class Summarize() : ActorMessage()
        class Rebuild() : ActorMessage()
    }

    /*
     * This is a specialized class used to render the per-merchant fields by the
     * ReceiptWatcher. For each merchant, we want to show the number of _consecutive_
     * payments processed for that merchant on a specific node. The TallyField class
     * takes care of rendering this information in a compact way, so that the user can
     * see the number of payments processed by for a merchant on each node in time
     * order.
     *
     * The class is initialized with the per-merchant value from the payment receipt
     * summary map tracked by the ReceiptWatcher.
     */
    class TallyField(
        private val width: Int,
        private val merchant: String,
        private val tally: List<NodeTally>
    ) {
        init {
            require(width >= merchant.numCodepoints() + 2)
        }

        /*
         * This is the public point of access for the class. It generates the string
         * representation for the per-merchant field.
         */
        fun generate(): String {
            val merchant = "${merchant}▹"
            val widthLeft = width - merchant.numCodepoints()
            val allButLast = tally.dropLast(1).map {
                "${fontEtched(it.onMember)}×${it.numProcessed}"
            }
            val last = tally.lastOrNull()?.let {
                "${fontEtched(it.onMember)}×${underline(it.numProcessed.toString())}"
            } ?: "NONE YET"
            val ticker = (allButLast + last).joinToString(" → ")
            return if (ticker.numCodepoints() > widthLeft) { // too big for field
                "$merchant⋯${ticker.trimStart(widthLeft - 1)}"
            } else {
                "$merchant${ticker.padStart(widthLeft)}"
            }
        }
    }

    /*
     * Use an actor approach for processing messages and modifying state. Only the
     * actor gets to modify the state encapsulated within this inner class, which
     * avoids mutable shared state. Since the event listener on the PaymentReceipt
     * map has to tell us about new receipts, it communicates this to the actor via
     * the ReceiptMessage sealed class.
     */
    private inner class ReceiptsActor() : AutoCloseable {
        /*
         * Scope used to start up our outFlow SharedFlow, which also collects our
         * inFlow. Therefore, this scope ends up governing both.
         */
        private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        // Get rid of scope on close.
        override fun close() {
            scope.cancel()
        }

        /*
         * This is where receipts are summarized, in a way that makes it easy to
         * report. We basically group by merchant, and for each merchant, we have a
         * series of NodeTally objects, one for each node that processed a receipt
         * for that merchant. Everything is stored in time order, so if there is a
         * topology change, then we'll see a switch, in some cases, from processing
         * receipts on one node to processing them on another.
         */
        private val receiptSummaryMap =
            sortedMapOf<String, MutableList<NodeTally>>()
        private var numReceipts = 0 // How many receipts have we summarized?

        // Keep track of the last node that processed each merchant.
        private val nodeLastUsedForMerchant = sortedMapOf<String, Int>()

        // This method clears all of the state listed above.
        fun reset() {
            receiptSummaryMap.clear()
            numReceipts = 0
            nodeLastUsedForMerchant.clear()
        }

        /* Process each new receipt into our map, so that the map tracks, for each
         * merchant, a list of nodes that receipts were paid on, in time order. This
         * function is only ever called sequentially by the single-threaded actor, so we
         * can modify the map, and count receipts, without using a mutex.
         */
        private fun mergeNewReceipt(receipt: PaymentReceipt) {
            val tally =
                receiptSummaryMap.getOrPut(receipt.merchantId) { mutableListOf() }
            val last = tally.lastOrNull()
            if (last?.onMember == receipt.onMember) {
                last.numProcessed++
            } else {
                // Add a new pair if tally is empty or the node is different
                tally.add(NodeTally(1, receipt.onMember))
                nodeLastUsedForMerchant[receipt.merchantId] = receipt.onMember
            }
            numReceipts++
        }

        /*
         * This function is only ever called, like the previous one, by the actor,
         * so the mutable state isn't shared, and we don't need a mutex.
         */
        private fun summarize(): Summary {
            // Update the nodes in use, so our failure simulator can target those
            nodesInUse.value = nodeLastUsedForMerchant.values.toList()

            // Capture snapshot of changing values from Kotlin mutableStateFlows
            val metricsSummary = getMetricsSummary(getNumPaymentsReceived())
            val numFields = merchantMap.size

            fun generateLogLine(fields: Iterable<String>): String =
                "$metricsSummary⟦ ${fields.joinToString(" | ")} ⟧"

            val prefixLen = 20
            val emptyFieldLine = generateLogLine(List(numFields) { "" })
            val fieldWidth =
                (screenWidth - emptyFieldLine.numCodepoints() - prefixLen) / numFields

            /* Now show, for each merchant, a tally of number of payments, and on which
             * node those payments were made on.
             */
            val merchantFields = merchantMap.values.map { merchant ->
                val tally = receiptSummaryMap[merchant.id] ?: emptyList()
                TallyField(fieldWidth, merchant.shortName, tally).generate()
            }

            return Summary(numReceipts, generateLogLine(merchantFields))
        }

        /*
         * Generate the initial part of the receipt summary, consisting of rolling
         * metrics. QUD = queued payments, WKG = in-process payments, FIN = finished
         * payments.
         */
        private fun getMetricsSummary(numReceived: Int) = mapOf(
            "QUD" to numPayments - numReceived,
            "WKG" to numReceived - numReceipts,
            "FIN" to numReceipts,
        ).entries.joinToString(" ⇒ ") { (metricName, metricValue) ->
            val paddedMetric =
                metricValue.toString().padStart(numPayments.toString().length, '0')
            "$paddedMetric $metricName"
        }.let { "[$it]➣" }

        /*
         * Finally, the following two Kotlin StateFlows are used to communicate with
         * the actor. The inFlow is used to send messages to the actor, and the outFlow
         * is used to receive the results of processing messages.
         */

        val inFlow =
            MutableSharedFlow<ActorMessage>(extraBufferCapacity = Channel.UNLIMITED)

        val outFlow = flow {
            var ignoreUpdates = false
            inFlow.collect { message ->
                when (message) {
                    is ActorMessage.Update -> {
                        if (!ignoreUpdates) mergeNewReceipt(message.receipt)
                    }

                    is ActorMessage.Summarize -> {
                        emit(summarize())
                    }

                    is ActorMessage.Rebuild -> {
                        ignoreUpdates = true
                        reset()
                        paymentReceiptMap.values.sorted().forEach { receipt ->
                            mergeNewReceipt(receipt)
                        }
                    }
                }
            }

            /* We need to replay 1 value to new collectors, because we're going to
             * send a final Summarize message to the actor at the end and a new
             * collector will fetch the result.
             */
        }.shareIn(scope, SharingStarted.Eagerly, 1)
    } // end of class ReceiptsActor()

    private val receiptsActor = ReceiptsActor() // Create the singleton.

    /*
     * Hazelcast EntryAddedListener (which derives MapListener) class. Hazelcast
     * will call into the entryAdded callback method, and that method therefore
     * needs to completely very quickly and reliably, and therefore it cannot
     * suspend. There will also be multiple calls simultaneously into entryAdded()
     * so we must use shared state in a thread-safe manner. We send each collected
     * event to a channel using trySend (this is threadsafe) which will make a
     * single attempt and always return.
     */
    private class ReceiptListener(
        private val actorFlow: MutableSharedFlow<ActorMessage>,
    ) : EntryAddedListener<Int, PaymentReceipt> {
        override fun entryAdded(event: EntryEvent<Int, PaymentReceipt>) {
            actorFlow.tryEmit(ActorMessage.Update(event.value))
        }
    }

    /* Register the EntryAddedListener with our payment receipt map.
     */
    private val listener = ReceiptListener(receiptsActor.inFlow)
    private val listenerUUID = paymentReceiptMap.addEntryListener(listener, true)

    override fun close() {
        paymentReceiptMap.removeEntryListener(listenerUUID)
        receiptsActor.close()
    }

    private suspend fun receiptsSummaryPoll() = with(receiptsActor.inFlow) {
        while (true) {
            delay(AppConfig.reportFrequency)
            emit(ActorMessage.Summarize())
        }
    }

    suspend fun startReceiptsLog() = coroutineScope {
        var previousNumReceipts = 0
        launch { receiptsSummaryPoll() }

        receiptsActor.outFlow.onEach { (_, receiptSummary) ->
            receiptSummary.let { if (it.isNotEmpty()) logger.log(it) }
        }.takeWhile { (numReceipts, _) -> numReceipts < numPayments }.onCompletion {
            this@coroutineScope.cancel()
        }.collect { (numReceipts, _) ->
            /* NOTE: Hazelcast MapListener Events are not treated as highly
             * available from a Hazelcast perspective, and as we are bringing nodes
             * down, we might miss a few messages. If it seems that our receipt
             * count isn't advancing at the end of the process, rebuild our data
             * from the source map.
             */
            if (numReceipts == previousNumReceipts && getNumPaymentsReceived() == numPayments) {
                with(receiptsActor.inFlow) {
                    emit(ActorMessage.Rebuild())
                    emit(ActorMessage.Summarize())
                }
            }
            previousNumReceipts = numReceipts
        }
    }
}
