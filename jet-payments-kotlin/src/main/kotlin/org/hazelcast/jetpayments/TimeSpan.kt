package org.hazelcast.jetpayments

/*
 * TimeSpan is a list ("span") of TimeRanges, along with a prefix for converting to
 * a string representation. Compare to CanvasSpan, within the Canvas class, which
 * consumes from TimeSpan.
 */
internal class TimeSpan(
    val prefix: String,
    val timeRanges: List<TimeRange>,
) : Comparable<TimeSpan> {
    init {
        require(!doRangesOverlap(timeRanges)) { "Overlapping ranges" }
        require(timeRanges.isNotEmpty()) { "Empty timeranges" }
    }

    val minTime = timeRanges.first().start
    val maxTime = timeRanges.last().endInclusive

    override fun compareTo(other: TimeSpan): Int = minTime.compareTo(other.minTime)
}
