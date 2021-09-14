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

package com.hazelcast.samples.jet.eventjournal;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import com.hazelcast.map.IMap;

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import static com.hazelcast.jet.pipeline.JournalInitialPosition.START_FROM_OLDEST;

/**
 * A pipeline which streams events from an IMap. The
 * values for new entries are extracted and then written
 * to a local IList in the Jet cluster.
 */
public class MapJournalSource {

    private static final String MAP_NAME = "map";
    private static final String SINK_NAME = "list";

    public static void main(String[] args) throws Exception {
        Config config = getConfig();
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        Hazelcast.newHazelcastInstance(config);
        JetService jet = hz.getJet();

        try {
            Pipeline p = Pipeline.create();
            p.readFrom(Sources.<Integer, Integer>mapJournal(MAP_NAME, START_FROM_OLDEST))
             .withoutTimestamps()
             .map(Entry::getValue)
             .writeTo(Sinks.list(SINK_NAME));

            jet.newJob(p);

            IMap<Integer, Integer> map = hz.getMap(MAP_NAME);
            for (int i = 0; i < 1000; i++) {
                map.set(i, i);
            }
            TimeUnit.SECONDS.sleep(3);
            System.out.println("Read " + hz.getList(SINK_NAME).size() + " entries from map journal.");
        } finally {
            Hazelcast.shutdownAll();
        }
    }


    private static Config getConfig() {
        Config cfg = new Config();
        // Add an event journal config for map which has custom capacity of 10_000
        // and time to live seconds as 10 seconds (default 0 which means infinite)
        cfg.getMapConfig(MAP_NAME)
           .getEventJournalConfig()
           .setEnabled(true)
           .setCapacity(10_000)
           .setTimeToLiveSeconds(10);
        cfg.getJetConfig().setEnabled(true);
        return cfg;
    }

}
