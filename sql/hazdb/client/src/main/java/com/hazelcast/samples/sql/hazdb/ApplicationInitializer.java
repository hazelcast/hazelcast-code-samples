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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.JobAlreadyExistsException;
import com.hazelcast.jet.Observable;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.datamodel.Tuple3;
import com.hazelcast.jet.pipeline.Pipeline;

/**
 * <p>
 * Initialize for this client.
 * </p>
 */
@Configuration
public class ApplicationInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationInitializer.class);

    @Autowired
    private HazelcastInstance hazelcastInstance;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            LOGGER.info("-=-=-=-=- '{}' initialization start -=-=-=-=-=-", this.hazelcastInstance.getName());

            boolean ok = true;
            ok = ok & this.createCDCJobs();

            if (ok) {
                LOGGER.info("-=-=-=-=- '{}' initialization end   -=-=-=-=-=-", this.hazelcastInstance.getName());
            } else {
                LOGGER.error("-=-=-=-=- '{}' initialization failed -=-=-=-=-=-", this.hazelcastInstance.getName());
                this.hazelcastInstance.shutdown();
            }
        };
    }

    /**
     * <p>Add CDC</p>
     *
     * @return
     */
    private boolean createCDCJobs() {
        // Destination needs to match Cdc.js
        String destination = "/" + MyLocalConstants.WEBSOCKET_TOPICS_PREFIX + "/cdc";

        for (String mapName : List.of(MyConstants.IMAP_NAME_BUNDESLIGA, MyConstants.IMAP_NAME_HEARTBEAT,
                MyConstants.IMAP_NAME_LEADER)) {
            Observable<Tuple3<String, String, String>> observable = this.hazelcastInstance.getJet().newObservable();
            observable.addObserver(tuple3 -> {
                try {
                    String payload = "{" + " \"timestamp\":\"" + ApplicationInitializer.isoDateNow() + "\""
                            + ",\"map\":\"" + tuple3.f0() + "\"" + ",\"event\":\"" + tuple3.f1() + "\"" + ",\"data\":\""
                            + tuple3.f2() + "\"" + "}";

                    LOGGER.debug("Send to websocket: '{}'", payload);
                    this.simpMessagingTemplate.convertAndSend(destination, payload);
                } catch (Exception e) {
                    LOGGER.error("createCDCJobs():observable:" + mapName, e);
                }
            });

            Pipeline pipeline = CdcPipeline.build(mapName, observable);

            String prefix = mapName + "-cdc";
            String suffix = ApplicationInitializer.isoDateNow();
            String jobName = prefix + "-" + suffix;
            this.checkJob(prefix, jobName);

            JobConfig jobConfig = new JobConfig();
            jobConfig.setName(jobName);

            try {
                Job job = this.hazelcastInstance.getJet().newJob(pipeline, jobConfig);
                LOGGER.info("Submitted: {}", job);
            } catch (JobAlreadyExistsException jaee) {
                // Unlike since job name is date based
                String message = String.format("createHeartbeatCDCJob(): '%s': %s", jobConfig.getName(),
                        jaee.getMessage());
                LOGGER.warn(message);
                return false;
            } catch (Exception e) {
                LOGGER.error("createCDCJobs():" + mapName, e);
                return false;
            }
        }

        return true;
    }

    /**
     * <p>
     * Check for running jobs with similar name
     * </p>
     *
     * @param prefix
     * @param jobName
     */
    private void checkJob(String prefix, String jobName) {
        for (Job job : this.hazelcastInstance.getJet().getJobs()) {
            if (job.getName() != null && job.getName().startsWith(prefix)) {
                LOGGER.warn("Intended job '{}' similar to running job '{}'", jobName, job);
            }
        }
    }

    private static String isoDateNow() {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }
}
