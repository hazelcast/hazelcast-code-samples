/*
 * Copyright (c) 2008-2021, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.samples.jet.jobmanagement;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;

import java.util.List;
import java.util.concurrent.CancellationException;

import static com.hazelcast.jet.pipeline.JournalInitialPosition.START_FROM_OLDEST;
import static java.util.Objects.requireNonNull;

/**
 * We demonstrate how submitted jobs can be fetched and tracked via any Jet
 * instance.
 */
public class JobTracking {

    public static void main(String[] args) {
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

        jet1.newJob(p, jobConfig);

        JetService jet2 = hz2.getJet();
        // jobs can be also tracked via other Jet nodes
        List<Job> jobs = jet2.getJobs();
        Job trackedJob1 = jobs.get(0);

        // job status can be queried via the tracked job object
        System.out.println("Tracked job: " + trackedJob1.getName() + ", status: " + trackedJob1.getStatus());

        // we can use the tracked job object to cancel the job
        trackedJob1.cancel();

        try {
            // let's wait until execution of the job is completed on the cluster
            trackedJob1.join();
            assert false;
        } catch (CancellationException e) {
            System.out.println("Job is cancelled.");
        }

        // Let's query the job status again. Now the status is FAILED.
        // It is expected status for streaming jobs when they are cancelled.
        System.out.println("Status: " + trackedJob1.getStatus());

        // running or completed jobs can be also queried by name
        Job trackedJob2 = requireNonNull(jet1.getJob(jobName));
        System.out.println("Tracked job status: " + trackedJob2.getStatus());

        hz1.getCluster().shutdown();
    }
}
