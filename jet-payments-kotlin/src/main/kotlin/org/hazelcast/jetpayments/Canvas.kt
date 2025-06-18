package org.hazelcast.jetpayments

import kotlin.math.floor
import kotlin.time.Duration

/*
 * A class that allows for sets of time ranges to be rendered as ASCII-art.
 */
internal open class Canvas(
    timespans: List<TimeSpan>, // provided in-order from top to bottom
    val canvasSize: Int = AppConfig.screenWidth - AppConfig.logPrefixLen - timespans.maxOf { it.prefix.numCodepoints() } - 8,
) {
    init {
        require(timespans.isNotEmpty()) { "Must be at least one input" }
    }

    private val canvasMinTime = timespans.minOf { it.minTime }
    private val canvasMaxTime = timespans.maxOf { it.maxTime }
    private val maxPrefixLen: Int = timespans.maxOf { it.prefix.numCodepoints() }
    private val totalCanvasTime = canvasMaxTime - canvasMinTime
    val timePerUnit = totalCanvasTime / canvasSize
    private val spans =
        listOf(generateTimescaleCanvasSpan()) + timespans.map { timespan ->
            CanvasSpan(timespan)
        }

    fun draw(): List<String> {
        require(spans.isNotEmpty()) { "Must be at least one input" }

        return TextBox(spans.map { span ->
            "${span.prefix.padStart(maxPrefixLen)} => │${span.draw()}│"
        }, borderStyle = TextBox.BorderStyle.THICK).toStrings()
    }

    protected open inner class CanvasSpan(timespan: TimeSpan) {
        val prefix = timespan.prefix

        protected fun Duration.toCanvasUnits() =
            floor((this - canvasMinTime) / timePerUnit).toInt()
                .coerceIn(0, canvasSize - 1)

        fun draw(): String {
            val row = buildString {
                ranges.fold(0) { cursor, span ->
                    if (cursor < span.start) append(".".repeat(span.start - cursor))
                    val lastPosition = span.endInclusive + 1
                    if (lastPosition <= canvasSize) append(span.draw())
                    lastPosition
                }.let { lastPosition ->
                    if (lastPosition < canvasSize) append(".".repeat(canvasSize - lastPosition))
                }
            }

            return row
        }

        private tailrec fun scaleAndFitRanges(
            timeRanges: List<TimeRange>,
            canvasRanges: List<StretchyCanvasRange> = listOf(),
        ): List<StretchyCanvasRange> {
            val firstTwoCanvasRanges = timeRanges.take(2).map { timeRange ->
                StretchyCanvasRange(timeRange) { timeRange ->
                    timeRange.toCanvasUnits()
                }.coerceAtMost(canvasSize - 1)
            }
            val firstRange =
                firstTwoCanvasRanges.firstOrNull() ?: return canvasRanges
            val secondRange = firstTwoCanvasRanges.drop(1).firstOrNull()
                ?: return canvasRanges + listOf(firstRange)
            val overlapsBy = firstRange.overlapsBy(secondRange)
            val newCanvasRanges = when (overlapsBy) {
                0 -> listOf(firstRange)

                in 1..(firstRange.length - 1) -> listOf(
                    firstRange.shrink(overlapsBy)
                )

                firstRange.length -> listOf()
                else -> throw IllegalStateException("Unexpected overlapsBy: $overlapsBy")
            }
            return scaleAndFitRanges(
                timeRanges.drop(1), canvasRanges + newCanvasRanges
            )
        }

        protected open fun timeRangesToCanvasRanges(timeRanges: List<TimeRange>): List<CanvasRange> {
            val ranges = scaleAndFitRanges(timeRanges)
            check(!doRangesOverlap(ranges)) { "Ranges overlap: $ranges" }
            return ranges
        }

        private val ranges = timeRangesToCanvasRanges(timespan.timeRanges)
    }

    private inner class FixedCanvasSpan(
        timespan: TimeSpan,
    ) : CanvasSpan(timespan) {
        override fun timeRangesToCanvasRanges(timeRanges: List<TimeRange>) =
            timeRanges.map { timerange ->
                // Center the marker at the timestamp
                FixedCanvasRange(
                    timerange.marker, timerange.start.toCanvasUnits()
                )
            }.filter { it.endInclusive < canvasSize } as List<CanvasRange>
    }

    /*
     * Builds a CanvasSpan representing the overall timeline, so that the user
     * can eyeball items on the canvas and estimate when they happened.
     */
    private fun generateTimescaleCanvasSpan(): FixedCanvasSpan {

        data class Ticks(
            val numDivs: Int,
            val divSize: Duration,
            val tickTimes: List<Duration>,
            val tickMarks: List<String>,
            val totalWidth: Int,
        )

        /* Iteratively find a division of the timeline into equal parts,
         * starting with 8 and reducing by 2 at a time, until we find a
         * division wherein we can fit all the tick markers into the timescale.
         */
        val minSpaceBetween = 4
        val ticks =
            generateSequence(8) { it - 2 }.takeWhile { it >= 2 }.map { numDivs ->
                val divSize = totalCanvasTime / numDivs
                val numTicks = numDivs + 1
                val tickTimes = List(numTicks) { index ->
                    canvasMinTime + divSize * index
                }
                val maxWidth = tickTimes.map { timestamp ->
                    timestamp.toWholeSecStr()
                }.maxOf { it.length }
                val tickMarks = tickTimes.map { timestamp ->
                    timestamp.toWholeSecStr()
                }.map { it.padStart(maxWidth, '0') }
                val totalWidth =
                    tickMarks.joinToString(" ".repeat(minSpaceBetween)).length /* space before & after */
                Ticks(numDivs, divSize, tickTimes, tickMarks, totalWidth)
            }.first { ticks ->
                /* Find the first (numDivs = number of divisions) within the sequence
                 * wherein the the total space taken up by *all* of the tick markers,
                 * plus padding on either side, fits within the canvas size. */
                ticks.totalWidth <= canvasSize && ticks.tickTimes.last() <= canvasMaxTime
            }

        // Center the tick marks
        val tickTimeRanges =
            ticks.tickMarks.zip(ticks.tickTimes).map { (marker, timestamp) ->
                (timePerUnit * marker.numCodepoints()).let { width ->
                    val start = (timestamp - width / 2)
                    val endInclusive = (start + width)
                    if (start < canvasMinTime) { // adjust right
                        TimeRange(marker, canvasMinTime, canvasMinTime + width)
                    } else if (endInclusive > canvasMaxTime) { // adjust left
                        TimeRange(marker, canvasMaxTime - width, canvasMaxTime)
                    } else {
                        TimeRange(marker, start, endInclusive)
                    }
                }
            }

        return FixedCanvasSpan(TimeSpan("TIMESCALE", tickTimeRanges))
    }

    protected abstract class CanvasRange(
        val marker: String,
        override val start: Int,
        override val endInclusive: Int,
    ) : ComparableClosedRange<Int>() {
        val length = endInclusive - start + 1

        init {
            require(start <= endInclusive) { "Invalid canvasrange: $start..$endInclusive" }
        }

        abstract fun draw(): String

        override fun toString(): String {
            return "($marker:$start..$endInclusive)"
        }

        fun overlapsBy(other: CanvasRange): Int = when {
            // No overlap
            !this.overlaps(other) -> 0
            // Their start is before or at my end
            other.start <= this.endInclusive -> this.endInclusive - other.start + 1
            // My start is at or after their end
            else -> this.start - other.endInclusive + 1
        }
    }

    class ScaleException(msg: String) : RuntimeException(msg)

    private class StretchyCanvasRange(
        marker: String,
        start: Int,
        endInclusive: Int,
    ) : CanvasRange(marker, start, endInclusive) {
        constructor(
            timerange: TimeRange,
            timestampToCanvasUnit: (Duration) -> Int,
        ) : this(
            timerange.marker,
            timestampToCanvasUnit(timerange.start),
            timestampToCanvasUnit(timerange.endInclusive),
        )

        override fun draw(): String {
            val leftBr = "["
            val rightBr = "]"
            val minimumStretchySize =
                leftBr.numCodepoints() + rightBr.numCodepoints() + 1

            fun drawStretchy(marker: String): String {
                require(length >= marker.numCodepoints() + 2) { "Invalid CanvasRange marker: $marker, $this" }
                val left = "=".repeat((length - (2 + marker.numCodepoints())) / 2)
                val right =
                    "=".repeat(length - (2 + marker.numCodepoints()) - left.length)
                return "$leftBr$left$marker$right$rightBr"
            }

            return when (length) {
                in 1 until minimumStretchySize -> marker.repeat(length)
                in minimumStretchySize..6 -> drawStretchy(marker)
                else -> drawStretchy(" $marker ")
            }
        }

        fun shrink(by: Int): StretchyCanvasRange {
            require(by >= 0) { "Invalid by=$by for $this" }
            if (by == 0) this
            if (start > endInclusive - by) throw ScaleException("Can't shrink $this by $by")
            return StretchyCanvasRange(marker, start, endInclusive - by)
        }

        fun coerceAtMost(atMost: Int) =
            if (endInclusive > atMost) shrink(endInclusive - atMost) else this
    }

    private class FixedCanvasRange(
        marker: String,
        start: Int,
    ) : CanvasRange(marker, start, start + marker.numCodepoints() - 1) {
        override fun draw(): String = marker
    }
}
