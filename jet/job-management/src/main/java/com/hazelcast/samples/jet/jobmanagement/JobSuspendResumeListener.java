package com.hazelcast.samples.jet.jobmanagement;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;

import java.util.concurrent.CancellationException;

import static com.hazelcast.jet.pipeline.JournalInitialPosition.START_FROM_OLDEST;

public class JobSuspendResumeListener {

    public static void main(String[] args) throws InterruptedException {
        // create two instances
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance();
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance();

        Pipeline p = Pipeline.create();
        p.readFrom(Sources.<Integer, Integer>mapJournal("source", START_FROM_OLDEST))
                .withoutTimestamps()
                .writeTo(Sinks.list("sink"));

        JetService jet1 = hz1.getJet();
        JobConfig jobConfig = new JobConfig();
        // job name is optional
        String jobName = "sampleJob";
        jobConfig.setName(jobName);
        Job job = jet1.newJob(p, jobConfig);

        // Register JobStatusListener
        job.addStatusListener(event ->
                System.out.printf("Job status changed: %s -> %s. User requested? %b%n",
                    event.getPreviousStatus(),
                    event.getNewStatus(),
                    event.isUserRequested())
        );

        // printing the job name
        System.out.println("Job '" + job.getName() + "' is submitted.");

        // we can suspend the job
        Thread.sleep(1000);
        System.out.println("Suspending the job...");
        job.suspend();

        // now, the job is not running and can be resumed later
        Thread.sleep(1000);
        System.out.println("Resuming the job...");
        job.resume();

        // we can cancel the job
        Thread.sleep(1000);
        System.out.println("Cancelling the job...");
        job.cancel();

        try {
            // let's wait until execution of the job is completed on the cluster
            // we can also call job.getFuture().get()
            job.join();
            assert false;
        } catch (CancellationException e) {
            System.out.println("Job is cancelled.");
        }

        hz1.getCluster().shutdown();
    }
}
