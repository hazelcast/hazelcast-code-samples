package org.hazelcast.jetpayments

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

/*
 * Abstract class for a streaming Jet pipeline. It has a runUntilCompleted() method
 * that runs the pipeline until it is done, and then terminates the streaming job.
 * Streaming Jet jobs don't terminate by themselves, they will run forever until
 * terminated. This class also has an abstract function called unitsLeft() that must
 * be implemented by subclasses, to tell this class how many units of work remain to
 * be done (where "unit" is defined by the subclass). Callers have to provide a
 * measure of the average amount of time taken per unit, so that this class can also
 * provide a "hasTimeLeft" method that can indicate whether the given amount of time
 * remains before the streaming job completes all units of work. This is used by the
 * FailureSimulator to determine whether enough time remains to execute a complete
 * failure cycle (bring a node down and back up again).
 */
abstract class StreamingJetPipeline(
    client: HzCluster.ClientInstance,
    jobName: String,
    private val timePerUnit: Duration,
) : JetPipeline(client, jobName) {

    abstract fun unitsLeft(): Int

    private fun isDone() = unitsLeft() < 1

    internal fun hasTimeLeft(timeLeft: Duration) =
        timePerUnit * unitsLeft() > timeLeft

    suspend fun runUntilCompleted() {
        coroutineScope {
            launch { run() }

            while (!isDone()) {
                delay(AppConfig.reportFrequency)
            }

            terminateJetJob()
        }
    }
}
