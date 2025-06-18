package org.hazelcast.jetpayments

/*
 * This is a specialized class used to render the per-merchant fields by the
 * ReceiptWatcher. For each merchant, we want to show the number of _consecutive_
 * payments processed for that merchant on a specific node. The TallyField class
 * takes care of rendering this information in a compact way, so that the user can
 * see the number of payments processed by for a merchant on each node in time
 * order.
 */
internal class TallyField(
    private val width: Int,
    private val merchantShortName: String,
    private val tally: List<ReceiptSummary.NodeTally>,
) {
    init {
        require(width >= merchantShortName.numCodepoints() + 2)
    }

    /*
     * This is the public point of access for the class. It generates the string
     * representation for the per-merchant field.
     */
    fun generate(): String {
        val merchant = "${merchantShortName}▹"
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
