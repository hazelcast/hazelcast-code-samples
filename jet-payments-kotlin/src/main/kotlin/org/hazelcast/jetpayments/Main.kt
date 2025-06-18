package org.hazelcast.jetpayments

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.hazelcast.jetpayments.AppConfig.paymentAmountRand
import org.hazelcast.jetpayments.AppConfig.paymentProcessingDelayRand
import org.hazelcast.jetpayments.AppConfig.paymentRequestDelayRand
import java.util.logging.Level
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

@Suppress("MayBeConstant")
internal object AppConfig {
    // Feel free to modify this section as you please.
    val clusterSize = 3
    val screenWidth = 174 // How wide should we draw in the log output
    val numMerchants = 4
    val warmupTime = 30.seconds // time before failures
    val cooldownTime = 15.seconds // time after failures
    val kafkaBootstrap: String = "localhost:9092"
    val kafkaPollTimeout = 10.seconds
    val jetScaleUpDelayMillis = 5000L
    val simulatePaymentProcessorFailure = false
    val reportFrequency = 5.seconds
    val seed = 9L
    val paymentRequestDelayRand = RandomNormalDist(100.0, 25.0, seed)
    val paymentProcessingDelayRand = RandomNormalDist(750.0, 250.0, seed)
    val paymentAmountRand = RandomNormalDist(75.0, 37.5, seed)
    val enableMemberLogs = false
    val logLevel: Level = Level.SEVERE
    val merchantIdWidth = 15
    val kafkaTopicName = "payment-requests"
    val kafkaProcessPaymentsCG = "initial-consumer-grp"
    val kafkaVerifyPaymentsCG = "verify-consumer-grp"
    val paymentProcessingJetJobName = "payment-processing-job"
    val paymentProcessedCheckJetJobName = "payment-processed-check-job"
    val paymentOnOneNodeCheckJetJobName = "payment-on-one-node-check-job"
    val uuidToMemberIndexMapName = "uuid-to-member-index-map"
    val paymentReceiptMapName = "payment-receipt-map"
    val paymentProcessedCheckListName = "payment-processed-check-list"
    val paymentOnOneNodeCheckMapName = "payment-on-one-node-check-map"

    // Following items generally needn't be changed. Modify with care!
    val steadyStateTime = 30.seconds
    private val jetMeanRescheduleTime = 10.seconds
    val failureCycleTime = (steadyStateTime + jetMeanRescheduleTime) * 2
    val jetStateHistorySize = 4
    val logPrefixLen = 24
    val displayWidth = screenWidth - logPrefixLen
}

internal val explainer = bold("EXPLAINER") + """
        => As payments flow in from Kafka, you'll see a stream of updates from the
        ReceiptsWatcher ("Watcher"). Each line will show you three metrics on these
        payments: QUD (number queued); WKG (number being processed); and FIN (number
        finished). After the metrics, you'll see a field for each of the various
        merchants (using the two-letter short name for the merchant; see table). At
        the right end of that field, you'll see something like
        "𝟙×${underline("5")}", which means node 𝟙 is processing that merchant's
        payments, and it has done 5 so far. The most recent count is always
        underlined. A failure simulator runs in the background to brings nodes down
        and up. As it does this, and Jet reschedules jobs to available nodes, you'll
        see this reflected in each merchant field; the existing data is shifted
        towards the left, as the newly scheduled node appears on the right.
    """

internal fun getJavaParams(): List<String> {
    return buildList {
        add(System.getProperty("java.vm.name"))
        add(System.getProperty("java.vendor"))
        add(System.getProperty("java.version"))
    }.let { list ->
        TextBox(list).center().addBorder().toStrings()
    }
}

// Main entry point. This is where the PaymentsRun is started.
fun main() {
    val logger = ElapsedTimeLogger("Main")

    // Show which VM we're using; display a helpful explainer about this demo.
    logger.log(getJavaParams())
    logger.log(TextBox(
        text = explainer.lines(), borderStyle = TextBox.BorderStyle.DOUBLE
    ).rewrap(AppConfig.screenWidth - AppConfig.logPrefixLen).toStrings())

    /* Start the PaymentsRun demo. Simulate two down/up failure cycles. runBlocking
     * is used to bridge between coroutines and the main thread. It will suspend
     * the main thread until the lambda passed to it returns.
     */
    runBlocking {
        PaymentsRun().run(2) // suspend function
    }
}

internal fun nextPaymentRequestDelay() = paymentRequestDelayRand.getValue().milliseconds

internal fun nextPaymentProcessingDelay() =
    paymentProcessingDelayRand.getValue().milliseconds

internal fun nextPaymentAmount() = paymentAmountRand.getValue().absoluteValue

/*
 * Randomness -> We want a single seed that we choose once, for all randomness
 */
internal val seededRandom = Random(AppConfig.seed)

/*
 * We want to measure time from a single baseline at the beginning of the test.
 * Encapsulate that here.
 */
internal object Epoch {
    private val theEpoch = MutableStateFlow(TimeSource.Monotonic.markNow())

    fun reset() {
        theEpoch.value = TimeSource.Monotonic.markNow()
    }

    fun timeNow() = theEpoch.value.elapsedNow()
}

/*
 * Miscellaneous helper functions.
 */

internal fun String.uniqify() =
    (1..4).map { (('a'..'z') + ('0'..'9')).random(seededRandom) }.joinToString("")
        .let { suffix ->
            "$this-$suffix"
        }

internal fun Duration.toWholeSecStr() =
    "${this.toDouble(DurationUnit.SECONDS).roundToInt()}s"

/*
 * Base class for ClosedRange<T> that implements Comparable<ClosedRange<T>>. This is
 * needed for the doRangesOverlap() function below. Note that ClosedRange<T> is a
 * Kotlin standard library interface, but it doesn't implement
 * Comparable<ClosedRange<T>>. So we create our own.
 */
internal abstract class ComparableClosedRange<T : Comparable<T>> : ClosedRange<T>,
    Comparable<ClosedRange<T>> {
    override fun compareTo(other: ClosedRange<T>) = this.start.compareTo(other.start)
}

// Does this ClosedRange overlap with the other?
internal fun <T : Comparable<T>> ClosedRange<T>.overlaps(other: ClosedRange<T>) =
    this.start <= other.endInclusive && this.endInclusive >= other.start

// Do any of the ClosedRanges in the list overlap?
internal fun <T : Comparable<T>> doRangesOverlap(ranges: Iterable<ComparableClosedRange<T>>) =
    ranges.sorted().zipWithNext().any { (first, second) ->
        first.overlaps(second)
    }

// Is this Collection already sorted?
internal fun <T : Comparable<T>> Collection<T>.isSorted() =
    zipWithNext { a, b -> a <= b }.all { it }

/*
 * Generalized function for combining a list of items into a condensed list.
 */
internal fun <T> List<T>.combine(
    isCombinable: (T, T) -> Boolean,
    combineWith: (T, T) -> T,
): List<T> {
    if (this.isEmpty()) return emptyList()
    val initial = mutableListOf(this.first())
    val rest = this.drop(1)
    return rest.fold(initial) { acc, curr ->
        acc.apply {
            val last = last()
            val toAdd = if (isCombinable(last, curr)) {
                removeLast()
                combineWith(last, curr)
            } else curr
            add(toAdd)
        }
    }
}

/*
 * Fold consecutive TimeRanges into a condensed set.
 */
internal fun foldTimeRanges(
    sortedTimeRanges: List<TimeRange>,
) = sortedTimeRanges.combine(TimeRange::isMergeable, TimeRange::merge)

/*
 * Use Java's Random class to create a normal distribution.
 */
internal class RandomNormalDist(
    val mean: Double,
    val stddev: Double,
    seed: Long,
) {
    private val javaRandom = java.util.Random(seed)
    fun getValue() = javaRandom.nextGaussian() * stddev + mean
}

/*
 * Remaining functions are all little helpers for manipulating strings.
 */

// Write this digit using an etched font.
internal fun fontEtched(digit: Int): String {
    require(digit in 0..9)
    val etched = listOf("𝟘", "𝟙", "𝟚", "𝟛", "𝟜", "𝟝", "𝟞", "𝟟", "𝟠", "𝟡").toTypedArray()
    return etched[digit % etched.size]
}

// Write this character using a bold font using a single codepoint.
internal fun fontBold(char: Char): String {
    require(char in 'A'..'Z')
    val boldChar = mapOf(
        'A' to "𝗔",
        'B' to "𝗕",
        'C' to "𝗖",
        'D' to "𝗗",
        'E' to "𝗘",
        'F' to "𝗙",
        'G' to "𝗚",
        'H' to "𝗛",
        'I' to "𝗜",
        'J' to "𝗝",
        'K' to "𝗞",
        'L' to "𝗟",
        'M' to "𝗠",
        'N' to "𝗡",
        'O' to "𝗢",
        'P' to "𝗣",
        'Q' to "𝗤",
        'R' to "𝗥",
        'S' to "𝗦",
        'T' to "𝗧",
        'U' to "𝗨",
        'V' to "𝗩",
        'W' to "𝗪",
        'X' to "𝗫",
        'Y' to "𝗬",
        'Z' to "𝗭"
    )
    return boldChar[char]!!
}

// Make this string bold using escape codes (8 added codepoints)
internal fun bold(str: String) = "\u001B[1m$str\u001B[0m"
internal fun italic(str: String) = "\u001B[3m$str\u001B[0m"
internal fun underline(str: String) = "\u001B[4m$str\u001B[0m"

/*
 * Extension function on String to count the number of Unicode codepoints. We'll use
 * a lot for Unicode strings in place of the length property of the String. In order
 * to count correctly, remove any escape sequences present first.
 */
internal fun String.numCodepoints() = this.replace("\u001B\\[\\d+m".toRegex(), "").let {
    it.codePointCount(0, it.length)
}

/*
 * Create our own versions of pad and trim functions. These already exist in
 * Kotlin's standard libraries, but they use *length*, and as many of the strings
 * we're logging are Unicode, we want to use numCodepoints() instead of length.
 */
internal fun String.pad(length: Int) = " ".repeat(length - this.numCodepoints())
internal fun String.padStart(length: Int) = pad(length) + this
internal fun String.padEnd(length: Int) = this + pad(length)
internal fun String.trimStart(length: Int): String {
    val numCodepoints = this.numCodepoints()
    return if (length >= numCodepoints) return this
    else this.substring(this.offsetByCodePoints(0, numCodepoints - length))
}

fun tabsToSpaces(s: String): String = s.replace("\t", "    ")
