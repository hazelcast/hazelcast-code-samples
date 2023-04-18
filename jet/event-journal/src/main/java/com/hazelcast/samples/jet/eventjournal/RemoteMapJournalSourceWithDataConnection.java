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
import com.hazelcast.config.DataConnectionConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.dataconnection.HazelcastDataConnection;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.jet.pipeline.DataConnectionRef;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import com.hazelcast.map.IMap;

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import static com.hazelcast.jet.pipeline.JournalInitialPosition.START_FROM_OLDEST;

/**
 * A pipeline which streams events from an IMap on a remote Hazelcast
 * cluster with a DataConnection. The values for new entries are extracted and then
 * written to a local IList in the Hazelcast cluster.
 */
public class RemoteMapJournalSourceWithDataConnection {

    private static final String REMOTE_CLUSTER_NAME = "remote_dev";
    private static final String REMOTE_MAP_NAME = "map";

    private static final String LOCAL_SINK_NAME = "list";

    private static final String HZ_CLIENT_DATA_CONNECTION_REF = "hz_client_data_connection_ref";

    public static void main(String[] args) throws Exception {
        HazelcastInstance remoteHz = startRemoteHzCluster();
        HazelcastInstance localHz = startLocalHzCluster();

        try {
            Pipeline pipeline = Pipeline.create();
            pipeline.readFrom(Sources.<Integer, Integer>remoteMapJournal(
                            REMOTE_MAP_NAME,
                            DataConnectionRef.dataConnectionRef(HZ_CLIENT_DATA_CONNECTION_REF),
                            START_FROM_OLDEST)
                    ).withoutTimestamps()
                    .map(Entry::getValue)
                    .writeTo(Sinks.list(LOCAL_SINK_NAME));

            JetService jetService = localHz.getJet();
            jetService.newJob(pipeline);

            // Write to remote map
            IMap<Integer, Integer> map = remoteHz.getMap(REMOTE_MAP_NAME);
            for (int i = 0; i < 1_000; i++) {
                map.set(i, i);
            }

            TimeUnit.SECONDS.sleep(3);

            // Read journal entries from local list
            System.out.println("Read " + localHz.getList(LOCAL_SINK_NAME).size() + " entries from remote map journal.");
        } finally {
            Hazelcast.shutdownAll();
        }

    }

    private static HazelcastInstance startLocalHzCluster() {
        Config localClusterConfig = getLocalClusterConfig();

        // Local cluster with two members
        HazelcastInstance localHz = Hazelcast.newHazelcastInstance(localClusterConfig);
        Hazelcast.newHazelcastInstance(localClusterConfig);
        return localHz;
    }

    private static Config getLocalClusterConfig() {
        Config config = new Config();
        config.setClusterName("jet");

        JetConfig jetConfig = config.getJetConfig();
        jetConfig.setEnabled(true);

        // Add DataConnectionConfig to remote cluster
        DataConnectionConfig dataConnectionConfig = new DataConnectionConfig(HZ_CLIENT_DATA_CONNECTION_REF);
        dataConnectionConfig.setType("HZ");

        String yamlString = "hazelcast-client:\n" +
                            "  cluster-name: " + REMOTE_CLUSTER_NAME + "\n" +
                            "  network:\n" +
                            "    cluster-members:\n" +
                            "      - 127.0.0.1:5701\n" +
                            "\n";
        dataConnectionConfig.setProperty(HazelcastDataConnection.CLIENT_YML, yamlString);

        config.addDataConnectionConfig(dataConnectionConfig);
        return config;
    }

    private static HazelcastInstance startRemoteHzCluster() {
        Config remoteClusterConfig = getRemoteClusterConfig();
        // Remote cluster with two members
        HazelcastInstance remoteHz = Hazelcast.newHazelcastInstance(remoteClusterConfig);
        Hazelcast.newHazelcastInstance(remoteClusterConfig);
        return remoteHz;
    }

    private static Config getRemoteClusterConfig() {
        Config config = new Config();
        // Add an event journal config for map which has custom capacity of 10_000
        // and time to live seconds as 10 seconds (default 0 which means infinite)
        config.getMapConfig(REMOTE_MAP_NAME)
                .getEventJournalConfig()
                .setEnabled(true)
                .setCapacity(10_000)
                .setTimeToLiveSeconds(10);
        config.getJetConfig().setEnabled(true);
        config.setClusterName(REMOTE_CLUSTER_NAME);
        return config;
    }

}
