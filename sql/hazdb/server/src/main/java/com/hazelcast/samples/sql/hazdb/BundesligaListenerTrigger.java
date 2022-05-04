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
import org.springframework.stereotype.Component;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

/**
 * <p>
 * Trigger a call to a stored procedure when data changes
 * </p>
 */
@Component
@SuppressWarnings("rawtypes")
public class BundesligaListenerTrigger implements EntryAddedListener, EntryRemovedListener, EntryUpdatedListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(BundesligaListenerTrigger.class);

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Override
    public void entryUpdated(EntryEvent arg0) {
        LOGGER.info("entryUpdated() - {}", arg0.getKey());
        this.process();
    }

    @Override
    public void entryRemoved(EntryEvent arg0) {
        LOGGER.info("entryRemoved() - {}", arg0.getKey());
        this.process();
    }

    @Override
    public void entryAdded(EntryEvent arg0) {
        LOGGER.info("entryAdded() - {}", arg0.getKey());
        this.process();
    }

    /**
     * <p>Trigger processing launches a stored procedure. Runs on one member in the cluster.
     * Other options exist to run on a specific member, a subset or all members.
     * </p>
     */
    private void process() {
        LeagueLeaderUpdateRunnable leagueLeaderUpdateRunnable
            = new LeagueLeaderUpdateRunnable();
        this.hazelcastInstance.getExecutorService("default").execute(leagueLeaderUpdateRunnable);
    }
}
