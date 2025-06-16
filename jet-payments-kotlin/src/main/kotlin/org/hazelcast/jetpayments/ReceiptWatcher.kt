package org.hazelcast.jetpayments

import com.hazelcast.core.EntryEvent
import com.hazelcast.map.IMap
import com.hazelcast.map.listener.EntryAddedListener
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlin.time.Duration

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
    merchantMap: Map<String, Merchant>,
    private val reportFrequency: Duration = AppConfig.reportFrequency,
    private val getNumIssued: () -> Int,
) : AutoCloseable {
    private val logger = ElapsedTimeLogger("Watcher")
    private val paymentReceiptMap =
        client.getMap<Int, PaymentReceipt>(AppConfig.paymentReceiptMapName)

    // Public state that reveals which nodes are currently processing receipts.
    val nodesInUse: MutableStateFlow<Set<Int>> = MutableStateFlow(emptySet())

    /*
     * Use an actor approach for processing messages and modifying state. Only the
     * actor gets to modify the state encapsulated within this inner class, which
     * avoids mutable shared state. Since the event listener on the PaymentReceipt
     * map has to tell us about new receipts, it communicates this to the actor via
     * the ReceiptMessage sealed class.
     */
    private class ReceiptsActor(
        private val numPayments: Int, private val merchantMap: Map<String, Merchant>
    ) : AutoCloseable,
        Channel<ReceiptsActor.Message> by Channel<Message>(Channel.UNLIMITED) {
        private val receiptSummary = ReceiptSummary()

        // Used when we summarize the state of the ReceiptSummary map to callers
        data class Summary(
            val numReceipts: Int, val nodesInUse: Set<Int>, val summaryString: String
        )

        /*
         * Data structures needed to communicate with the ReceiptsActor.
         */
        sealed class Message { // input
            class Update(val receipt: PaymentReceipt) : Message()

            class Summarize(
                val numIssued: Int, val outbox: MutableSharedFlow<Summary>
            ) : Message()

            class Rebuild(val paymentReceiptMap: IMap<Int, PaymentReceipt>) : Message()
        }

        /*
         * Methods to perform input/output to Actor from outside
         */
        private suspend fun processMessages() {
            for (message in this) {
                when (message) {
                    is Message.Update -> receiptSummary.addReceipt(message.receipt)
                    is Message.Summarize -> message.outbox.emit(summarize(message.numIssued))
                    is Message.Rebuild -> receiptSummary.rebuildFromMap(message.paymentReceiptMap)
                }
            }
        }

        val scope = CoroutineScope(Dispatchers.Default).run {
            launch {
                processMessages()
            }
        }

        override fun close() {
            scope.cancel()
        }

        /*
         * Generate the initial part of the receipt summary, consisting of rolling
         * metrics. QUD = queued payments, WKG = in-process payments, FIN = finished
         * payments.
         */
        private fun getMetricsSummary(numIssued: Int): String {
            val numReceipts = receiptSummary.numReceipts

            val widest = numPayments.toString().length // numPayments is biggest
            val metrics = mapOf(
                "QUD" to numPayments - numIssued,
                "WKG" to numIssued - numReceipts,
                "FIN" to numReceipts,
            ).entries.joinToString(" ⇒ ") { (name, value) ->
                "${value.toString().padStart(widest, '0')} $name"
            }

            return "[$metrics]➣"
        }

        /*
         * This function is only ever called, like the previous one, by the actor,
         * so the mutable state isn't shared, and we don't need a mutex.
         */
        private fun summarize(numIssued: Int): Summary {
            val metricsSummary = getMetricsSummary(numIssued)
            fun generateLogLine(fields: Iterable<String>): String =
                "$metricsSummary⟦ ${fields.joinToString(" | ")} ⟧"

            val numMerchants = merchantMap.size
            val minimal = generateLogLine(List(numMerchants) { "" })
            val availableSpace = AppConfig.displayWidth - minimal.numCodepoints()
            val fieldWidth = availableSpace / numMerchants

            /*
             * Now show, for each merchant, a list of all the payments grouped
             * chronologically by the node on which payments were processed, and how
             * many were processed on that node before another node was selected.
             */
            val merchantFields = merchantMap.values.map { merchant ->
                val tally = receiptSummary.getTallyForMerchant(merchant.id)
                TallyField(fieldWidth, merchant.shortName, tally).generate()
            }

            return Summary(
                receiptSummary.numReceipts,
                receiptSummary.nodesInUse,
                generateLogLine(merchantFields)
            )
        }
    }

    private val receiptsActor = ReceiptsActor(numPayments, merchantMap)

    /*
     * Hazelcast EntryAddedListener (which derives MapListener) object. Hazelcast
     * will call into the entryAdded callback method, and that method therefore
     * needs to completely very quickly and reliably, and cannot suspend. There will
     * also be multiple simulaneous calls into entryAdded(), so we use a Channel
     * which is thread-safe.
     */
    private val listenerUuid = paymentReceiptMap.addEntryListener(
        object : EntryAddedListener<Int, PaymentReceipt> {
            override fun entryAdded(event: EntryEvent<Int, PaymentReceipt>) {
                receiptsActor.trySend(ReceiptsActor.Message.Update(event.value))
            }
        }, true
    )

    override fun close() {
        paymentReceiptMap.removeEntryListener(listenerUuid)
    }

    /*
     * Used below to keep track of the most-recent, and 2nd-most-recent
     * captures of the number of receipts; we want to compare these to
     * see if they're still changing. For convenience we also track:
     * nodesInUse, so we can update this value in the surrounding class;
     * and summaryString, for periodic display to the ReceiptWatcher log
     */
    private data class SummaryTracker(
        val lastNumReceipts: Int = 0,
        val numReceipts: Int = 0,
        val nodesInUse: Set<Int> = emptySet(),
        val summaryString: String = "",
    ) {
        fun hasUpdated() = lastNumReceipts != numReceipts
    }

    suspend fun startReceiptsLog(): Unit = coroutineScope {
        val outbox = MutableSharedFlow<ReceiptsActor.Summary>(
            replay = 0, extraBufferCapacity = Channel.UNLIMITED
        )

        val summarizeJob = launch {
            while (true) {
                val msg = ReceiptsActor.Message.Summarize(getNumIssued(), outbox)
                receiptsActor.send(msg)
                delay(reportFrequency)
            }
        }

        /*
         * Keep track of the 2nd-latest and latest reports of the number
         * of receipts in the payments summary map, using a scan()
         * function that traverses the output from the receiptsActor.
         */
        outbox.scan(SummaryTracker()) { tracker, next ->
            SummaryTracker(
                lastNumReceipts = tracker.numReceipts, // age this field
                numReceipts = next.numReceipts, // get the latest one
                nodesInUse = next.nodesInUse,
                summaryString = next.summaryString,
            )
        }.drop(1).onEach { tracker ->
            nodesInUse.value = tracker.nodesInUse
            if (tracker.hasUpdated()) logger.log(tracker.summaryString)
        }.takeWhile { tracker -> tracker.numReceipts < numPayments }.onCompletion {
            summarizeJob.cancel()
        }.filter { tracker -> !tracker.hasUpdated() }.collect {
            /*
             * If we've gotten here, then the receipt count isn't advancing,
             * which has happened because we've missed some MapListener events
             * (they're not guaranteed during cluster topology changes).
             * If all payments have been issued, indicating the end of
             * the run, force a rebuild of our data from the source map.
             */
            if (getNumIssued() >= numPayments) {
                receiptsActor.send(ReceiptsActor.Message.Rebuild(paymentReceiptMap))
                receiptsActor.send(
                    ReceiptsActor.Message.Summarize(
                        getNumIssued(), outbox
                    )
                )
            }
        }
    }
}
