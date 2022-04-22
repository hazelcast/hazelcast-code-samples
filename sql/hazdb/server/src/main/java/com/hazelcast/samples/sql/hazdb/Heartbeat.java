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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hazelcast.cluster.Address;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.map.IMap;

/**
 * <p>
 * Periodically save stats to a {@link com.hazelcast.map.IMap IMap}
 * </p>
 */
@Component
@EnableScheduling
public class Heartbeat {
    private static final Logger LOGGER = LoggerFactory.getLogger(Heartbeat.class);

    @Autowired
    private HazelcastInstance hazelcastInstance;

    /**
     * <p>
     * Run once a minute once warmed up.
     * </p>
     */
    @Scheduled(initialDelay = 10_000, fixedDelay = 60_000)
    public void heartbeat() {
        try {
            if (this.hazelcastInstance.getLifecycleService().isRunning()) {
                IMap<HazelcastJsonValue, HazelcastJsonValue> heartbeatMap = this.hazelcastInstance
                        .getMap(MyConstants.IMAP_NAME_HEARTBEAT);

                Address address = this.hazelcastInstance.getCluster().getLocalMember().getAddress();

                String node = address.getHost() + ":" + address.getPort();
                HazelcastJsonValue key = new HazelcastJsonValue(
                        "{" + " \"node\": \"" + node + "\"" + ", \"timestamp\": " + System.currentTimeMillis() + "}");

                HazelcastJsonValue value = new HazelcastJsonValue(
                        "{" + "  \"freeMemory\": " + Runtime.getRuntime().freeMemory() + ", \"usedMemory\": "
                                + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "}");

                LOGGER.debug("'{}'=='{}'", key, value);
                heartbeatMap.put(key, value);
            }
        } catch (Exception e) {
            LOGGER.error("heartbeat()", e);
        }
    }
}

