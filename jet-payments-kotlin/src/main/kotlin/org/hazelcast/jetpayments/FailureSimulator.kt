package org.hazelcast.jetpayments

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

/*
 * Simulate the failure and recovery of nodes in the Hazelcast cluster so we can
 * observe how Jet gracefully responds to these. The failure simulator is run on
 * a separate coroutine, asynchronously, using the handed-in HzCluster to bring
 * nodes down and back up. Keeps track of the failure periods so that these can
 * be graphed using the Canvas object later.
 */
internal class FailureSimulator(
    private val client: HzCluster.ClientInstance,
    private val getNodesInUse: () -> List<Int>
) {
    private val logger = ElapsedTimeLogger("FailSim")

    enum class NodeCycleRequest {
        SHUTDOWN_NODE, RESTORE_NODE
    }

    // Need to be cleared on every run
    private val eventTimeRanges =
        Array(client.originalClusterSize) { mutableListOf<TimeRange>() }

    /*
     * Suspending call to run all the failure simulations for this payment run. The
     * flow we're collecting here will stop emitting when we don't have enough time
     * to run any more complete failure simulation cycles (bringing a node down,
     * *and* back up again). Whether we have enough time, is determined from the
     * pipelineHasTimeLeft() function passed in here. When all down/up cycles have
     * finished, this method will return a list of TimeSpans, showing when nodes
     * were brown down or up, measuring from the point from initiating the topology
     * change to the point where Jet has rescheduled work across members according
     * to the new topology.
     */
    suspend fun runSimulations(
        jetSchedulerStateFlow: StateFlow<JetPipeline.JetSchedulerState>,
        pipelineHasTimeLeft: (Duration) -> Boolean, // Do we have this much time?
    ): List<TimeSpan> {
        failureFlow(pipelineHasTimeLeft).collect { (nodeCycleRequest, member) ->
            fun emph(str: String) = "/=> ${italic(str)} <=/"
            val marker = when (nodeCycleRequest) {
                NodeCycleRequest.SHUTDOWN_NODE -> "↓"
                NodeCycleRequest.RESTORE_NODE -> "↑"
            }

            val reqName = nodeCycleRequest.name
            val memberName = fontEtched(member)
            logger.log(emph("Starting $reqName $memberName"))

            // Adjust cluster topology and note the timeNow time for it.
            val start = Epoch.timeNow()
            adjustClusterState(nodeCycleRequest, member, jetSchedulerStateFlow)
            val end = Epoch.timeNow()
            eventTimeRanges[member].add(TimeRange(marker, start, end))

            logger.log(emph("Finished $reqName $memberName"))
        }
        return getTimeSpans()
    }

    /*
     * We call this after all the failure simulations are complete, in order to get
     * the measured time of the failure cycles so we can graph these.
     */
    private fun getTimeSpans() =
        eventTimeRanges.mapIndexed { member, timeRanges -> member to timeRanges }
            .filter { it.second.isNotEmpty() }.map { (member, timeRanges) ->
                TimeSpan("NODE ${fontEtched(member)}", timeRanges)
            }

    /*
     * Bring a given node down, or back up. See comments for JetScheduleState. We
     * track four states for Jet, in terms of whether or not is has rescheduled
     * subsequent to a node entering or leaving the cluster. Here we only care about
     * two of those states, which both relate to the cases where Jet has already
     * noticed any recent topology changes and has rescheduled jobs accordingly.
     */
    private suspend fun adjustClusterState(
        nodeCycleRequest: NodeCycleRequest,
        member: Int,
        jetSchedulerStateFlow: StateFlow<JetPipeline.JetSchedulerState>
    ) {
        when (nodeCycleRequest) {
            NodeCycleRequest.SHUTDOWN_NODE -> {
                jetSchedulerStateFlow.first { it is JetPipeline.JetSchedulerState.Running }
                client.shutdownMember(member)
                jetSchedulerStateFlow.first { it is JetPipeline.JetSchedulerState.RunningWithNodeDown }
            }

            NodeCycleRequest.RESTORE_NODE -> {
                jetSchedulerStateFlow.first { it is JetPipeline.JetSchedulerState.RunningWithNodeDown }
                client.restartMember(member)
                jetSchedulerStateFlow.first { it is JetPipeline.JetSchedulerState.Running }
            }
        }
    }

    /*
     * This function creates a Kotlin floW that contains both the uptime/downtime
     * events themselves, along with a period of delay between each. These are
     * consumed by the runSimulations method above, which does a collect on the
     * flow, and responds to the events by calling adjustClusterState to bring nodes
     * down or back up again. Thus it is this function, failureFlow, that provides
     * the overall timing and rhythm for the failure simulator.
     */
    private fun failureFlow(pipelineHasTimeLeft: (Duration) -> Boolean) = flow {
        delay(AppConfig.warmupTime)
        val alreadySelected = mutableSetOf<Int>()

        /* Always do the DOWN and UP as a pair. If we don't have enough time
         * left to do the full cycle, then don't start one.
         */
        while (pipelineHasTimeLeft(AppConfig.failureCycleTime)) {
            delay(AppConfig.steadyStateTime) // steadyStateTime
            val memberToKill = getNodesInUse().toSet().let { available ->
                if (available.isEmpty()) {
                    (0 until client.originalClusterSize).random(seededRandom)
                } else {
                    val unselected = available - alreadySelected
                    if (unselected.isNotEmpty()) {
                        unselected.random(seededRandom)
                    } else {
                        available.random(seededRandom)
                    }
                }
            }
            alreadySelected.add(memberToKill)
            emit(NodeCycleRequest.SHUTDOWN_NODE to memberToKill) // 4 * snapshot
            delay(AppConfig.steadyStateTime) // steadyStateTime
            emit(NodeCycleRequest.RESTORE_NODE to memberToKill) // 4 * snapshot
        }
    }
}
