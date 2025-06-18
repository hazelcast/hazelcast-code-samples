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
        require(timeRanges.isNotEmpty()) { "Must supply some TimeRanges" }
        require(!doRangesOverlap(timeRanges)) { "TimeRanges must be non-overlapping" }
        require(timeRanges.isSorted()) { "TimeRanges must be sorted by start time" }
    }

    val minTime = timeRanges.first().start
    val maxTime = timeRanges.last().endInclusive

    override fun compareTo(other: TimeSpan): Int = minTime.compareTo(other.minTime)
}
