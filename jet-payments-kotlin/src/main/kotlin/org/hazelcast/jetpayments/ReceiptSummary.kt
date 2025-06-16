package org.hazelcast.jetpayments

import com.hazelcast.map.IMap
import kotlin.collections.set

/*
 * This class is not threadsafe, and therefore requires
 * an external means of serializing access to methods.
 */
internal class ReceiptSummary {
    // Data class that tracks the number of receipts processed on a particular node.
    data class NodeTally(val numProcessed: Int, val onMember: Int)

    /*
     * This is where receipts are summarized, in a way that makes it easy to
     * report. We basically group by merchant, and for each merchant, we have a
     * series of NodeTally objects, one for each node that processed a receipt
     * for that merchant. Everything is stored in time order, so if there is a
     * topology change, then we'll see a switch, in some cases, from processing
     * receipts on one node to processing them on another.
     */
    private val receiptSummaryMap = sortedMapOf<String, List<NodeTally>>()
    var numReceipts = 0 // How many receipts have we summarized?
        private set

    /* Process each new receipt into our map, so that the map tracks, for each
     * merchant, a list of nodes that receipts were paid on, in time order.
     */
    fun addReceipt(receipt: PaymentReceipt) {
        val tally = receiptSummaryMap.getOrPut(receipt.merchantId) { emptyList() }
        val last = tally.lastOrNull()
        val newTally = if (last?.onMember == receipt.onMember) {
            tally.dropLast(1) + NodeTally(last.numProcessed + 1, receipt.onMember)
        } else {
            // Add a new pair if tally is empty or the node is different
            tally + NodeTally(1, receipt.onMember)
        }
        receiptSummaryMap[receipt.merchantId] = newTally
        numReceipts++
    }

    fun getTallyForMerchant(merchantId: String) =
        receiptSummaryMap[merchantId] ?: emptyList()

    val nodesInUse: Set<Int>
        get() = receiptSummaryMap.values.mapNotNull { tallyList ->
            tallyList.lastOrNull()?.onMember
        }.toSet()

    fun rebuildFromMap(paymentReceiptMap: IMap<Int, PaymentReceipt>) {
        receiptSummaryMap.clear()
        numReceipts = 0

        paymentReceiptMap.values.groupBy { it.merchantId }.mapValues { (_, receipts) ->
            receipts.sorted().map { receipt ->
                NodeTally(1, receipt.onMember)
            }.combine({ tally1, tally2 ->
                tally1.onMember == tally2.onMember
            }, { tally1: NodeTally, tally2: NodeTally ->
                NodeTally(
                    tally1.numProcessed + tally2.numProcessed, tally1.onMember
                )
            })
        }.forEach { (merchant, tallyList) ->
            receiptSummaryMap[merchant] = tallyList
            numReceipts += tallyList.sumOf { it.numProcessed }
        }
    }
}