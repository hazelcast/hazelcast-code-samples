/*
 * Copyright (c) 2008-2022, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.samples.sql.hazdb;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Job;
import com.hazelcast.map.IMap;

/**
 * <p>
 * Periodic logging of data volumes.
 * </p>
 */
@Component
@EnableScheduling
public class Monitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Monitor.class);

    @Autowired
    private HazelcastInstance hazelcastInstance;
    private int count;

    /**
     * <p>
     * Run once a minute once warmed up.
     * </p>
     */
    @Scheduled(initialDelay = 10_000, fixedDelay = 60_000)
    public void monitor() {
        try {
            if (this.hazelcastInstance.getLifecycleService().isRunning()) {
                String countStr = String.format("%05d", this.count);
                LOGGER.info("-=-=-=-=- {} '{}' {} -=-=-=-=-=-", countStr, this.hazelcastInstance.getName(), countStr);

                this.logSizes();
                this.logJobs();
                this.count++;
            }
        } catch (Exception e) {
            LOGGER.error("monitor()", e);
        }
    }

    /**
     * <p>
     * "{@code size()}" is a relatively expensive operation, but we don't run it
     * frequently.
     * </p>
     */
    private void logSizes() {
        Set<String> mapNames = this.hazelcastInstance.getDistributedObjects().stream()
                .filter(distributedObject -> (distributedObject instanceof IMap))
                .filter(distributedObject -> !distributedObject.getName().startsWith("__"))
                .map(distributedObject -> distributedObject.getName()).collect(Collectors.toCollection(TreeSet::new));

        if (mapNames.isEmpty()) {
            LOGGER.info("No maps");
        } else {
            mapNames.forEach(name -> {
                IMap<?, ?> iMap = this.hazelcastInstance.getMap(name);
                LOGGER.info("MAP '{}'.size() => {}", iMap.getName(), iMap.size());
            });
        }
    }

    /**
     * <p>
     * List running processing pipelines.
     * </p>
     */
    private void logJobs() {
        Map<String, Job> jobs = new TreeMap<>();
        this.hazelcastInstance.getJet().getJobs().stream().forEach(job -> {
            if (job.getName() != null) {
                jobs.put(job.getName(), job);
            }
        });

        if (jobs.size() == 0) {
            LOGGER.info("No jobs");
        } else {
            jobs.forEach((key, value) -> {
                LOGGER.info("JOB '{}' => {}", key, value.getStatus());
            });
        }
    }
}
