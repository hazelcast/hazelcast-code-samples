package org.hazelcast.jetpayments

import com.hazelcast.jet.config.JobConfig
import com.hazelcast.jet.config.ProcessingGuarantee
import com.hazelcast.jet.core.JobStatus
import com.hazelcast.jet.pipeline.Pipeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import com.hazelcast.jet.Job as HzJob

/*
 * Class used for starting and managing Hazelcast Jet pipelines. The logic for the
 * Jet job is defined in subclasses of this one, and handed 'up' via the
 * createPipeline() abstract method, which is invoked in startPipeline. This class
 * manages the lifecycle of the jet job. It creates a jet job status listener to
 * track the state changes of the job, and respond appropriately.
 */
abstract class JetPipeline(
    private val client: HzCluster.ClientInstance,
    private val jobName: String,
) : AutoCloseable {
    protected val logger = ElapsedTimeLogger("JetPipeline")
    private var jetJob: HzJob? = null
    private val jetJobStateFlow = MutableStateFlow(JobStatus.RUNNING)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /* This is a state machine for tracking whether Jet has noticed a topology
     * change and scaled up/down jobs accordingly. These are "synthetic" states; we
     * generate these here from actual Jet state, as well as the cluster topology;
     * this is not real Hazelcast state. There are four states, always in order. A
     * StateFlow of the JetSchedulerState is consumed by the failure simulator, to
     * track the time period between the initiation of the shutdown or restoration
     * of a node, and the moment that Jet has responded to this by rebalancing work
     * to exclude the failed node or add in the recovered node. Technically we could
     * get by with just two states, "Running" and "JetNeedsReschedule", but we've
     * split those into two, one for "post-fail" and one for "post-recovery", in
     * order to avoid ambiguity in the Failure Simulator which looks at these
     * states.
     */
    sealed class JetSchedulerState {
        object Running : JetSchedulerState()
        object WaitingForJetToEvictNode : JetSchedulerState()
        object RunningWithNodeDown : JetSchedulerState()
        object WaitingForJetToAddNode : JetSchedulerState()

        companion object {
            val initialState = Running as JetSchedulerState
        }

        override fun toString(): String = this::class.simpleName ?: super.toString()

        fun transition(
            isJetRunning: Boolean, isNodeDown: Boolean
        ): JetSchedulerState = when (this) {
            is Running -> if (isNodeDown || !isJetRunning) WaitingForJetToEvictNode else this
            is WaitingForJetToEvictNode -> if (isNodeDown && isJetRunning) RunningWithNodeDown else this
            is RunningWithNodeDown -> if (!isNodeDown || !isJetRunning) WaitingForJetToAddNode else this
            is WaitingForJetToAddNode -> if (!isNodeDown && isJetRunning) Running else this
        }
    }

    /*
     * See state machine above. The following code represents a StateFlow that
     * observes the state of (1) the jet job and (2) the cluster topology, and
     * responds by transitioning the state machine above accordingly.
     */

    data class JetState(val jobStatus: JobStatus, val numMembers: Int) {
        override fun toString(): String = "$jobStatus($numMembers)"
    }

    private val initialJetState = JetState(
        JobStatus.RUNNING, client.originalClusterSize
    )

    class JetStateTracker() {
        private val hist = RingBuffer<JetState>(AppConfig.jetStateHistorySize)

        fun add(jetState: JetState): JetStateTracker = this.apply {
            hist.addRemoving(jetState)
            check(hist.size > 0 && hist.size <= AppConfig.jetStateHistorySize)
        }

        override fun toString(): String = hist.toString()
    }

    /*
     * This first StateFlow simply captures a combination of the Jet job status and
     * the number of members in the cluster, both captured together at the same
     * instant. We also have embedded a tracking object here for debugging, which
     * captures the historical record of JetStateFlow. The last map drops the
     * tracker, as we only want to emit the JetState to the subsequent flow.
     */
    private val jetStateFlow = combine(
        jetJobStateFlow, client.membershipListener.numMembersFlow
    ) { jobStatus, numMembers ->
        JetState(jobStatus, numMembers)
    }.scan(initialJetState to JetStateTracker()) { (_, tracker), newJetState ->
        newJetState to tracker.add(newJetState) // keep tracker around for debug
    }.map { (jetState, _) ->
        jetState
    }.stateIn( // eager StateFlow, which starts collecting immediately
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = initialJetState
    )

    /*
     * This next StateFlow takes the JetState from the previous StateFlow, and uses
     * it to transition the state machine above. It is used by the Failure Simulator
     * to track the time period between the initiation of the shutdown or
     * restoration of a node, and the moment that Jet has responded to this by
     * rebalancing work to exclude the failed node or add in the recovered node.
     */
    val jetSchedulerStateFlow =
        jetStateFlow.scan(JetSchedulerState.initialState) { state, jetState ->
            state.transition(
                jetState.jobStatus == JobStatus.RUNNING,
                jetState.numMembers < client.originalClusterSize
            )
        }.stateIn( // lazy StateFlow waits for first subscriber
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = JetSchedulerState.initialState
        )

    /*
     * Manually terminate the Jet job. Used on close(), and always used for
     * streaming Jet jobs to complete them.
     */
    fun terminateJetJob() {
        if (jetJob?.status in setOf(
                JobStatus.RUNNING, JobStatus.SUSPENDED, JobStatus.STARTING
            )
        ) jetJob?.cancel() // Don't wait on the job; cancel it immediately
        jetJob = null
    }

    override fun close() {
        terminateJetJob()
        scope.cancel()
    }

    protected abstract fun describePipeline(): String // Documentation only
    protected abstract val pipeline: Pipeline // The actual Jet Pipeline

    /*
     * Run the Jet pipeline. If it's a batch job, this function will return when it
     * completes. If it's a streaming job, then the job needs to be externally
     * terminated by calling terminateJetJob(). This method will spot that the Jet
     * job has terminated and it will then return.
     */
    suspend fun run() {
        // Provide some helpful info on the Jet pipeline to the user
        TextBox(bold(italic("Starting Jet job $jobName"))).stack(
            TextBox(pipeline.toDotString().lines()).addBorder()
        ).let { left ->
            val wrapLen =
                AppConfig.screenWidth - AppConfig.logPrefixLen - left.width - 8
            val right = TextBox(describePipeline().lines()).rewrap(wrapLen)
            left.adjoin(right).addBorder().log(logger)
        }

        // Configure the Jet job
        val jetJobConfig = JobConfig().apply {
            name = jobName
            processingGuarantee = ProcessingGuarantee.EXACTLY_ONCE
            isSuspendOnFailure = true
        }

        /* Now start the Jet pipeline, adding a Status Listener to capture back
         * the changing status of the Jet job as it processes.
         */
        jetJob = client.jet.newJob(pipeline, jetJobConfig).apply {
            addStatusListener { event ->
                jetJobStateFlow.tryEmit(event.newStatus) // can't fail to emit
            }
        }

        /* Return when the Jet job completes. The call to first() here results in a
         * collect(), which will suspend until it can find an emitted status from
         * the jetJobStateFlow that matches one in the list provided.
         */
        jetJobStateFlow.first { status ->
            status in setOf(
                JobStatus.FAILED, JobStatus.SUSPENDED, JobStatus.COMPLETED
            )
        }
    }
}
