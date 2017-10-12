/*
 *
 *  Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.logging.ILogger;

import static com.hazelcast.config.MapStoreConfig.InitialLoadMode.EAGER;

public class ClientDynamicConfig {

    /*
     * Starts a Hazelcast member and client without any explicit data structure configuration, then adds
     * dynamic data structure configuration from a client
     */
    public static void main(String[] args) {
        // Start a member with no explicit configuration
        HazelcastInstance instance = Hazelcast.newHazelcastInstance();
        ILogger logger = instance.getLoggingService().getLogger(ClientDynamicConfig.class);

        // Start a client -- we will be using the map to access & configure data structures
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        // Obtain a map
        IMap<String, String> defaultMap = client.getMap("defaultMap");
        // no data has been added yet, so get("1") will return null
        logger.info("defaultMap[\"1\"] = " + defaultMap.get("1"));

        // Another application will be using a map with a map loader and no backups
        // Wildcards also work for dynamically added data structure configurations
        MapConfig mapWithLoaderConfig = new MapConfig("map-with-loader-*").setBackupCount(0);
        mapWithLoaderConfig.getMapStoreConfig()
                           .setEnabled(true)
                           .setInitialLoadMode(EAGER)
                           .setClassName("EchoMapLoader");

        // add the configuration to the already running member
        client.getConfig().addMapConfig(mapWithLoaderConfig);

        IMap<String, String> mapWithLoader1 = client.getMap("map-with-loader-1");
        logger.info("mapWithLoader1[\"1\"] = " + mapWithLoader1.get("1") + " (loaded from configured map loader)");

        client.shutdown();
        instance.shutdown();
    }

}
