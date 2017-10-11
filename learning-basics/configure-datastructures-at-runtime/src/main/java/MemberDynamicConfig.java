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

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.logging.ILogger;

import static com.hazelcast.config.MapStoreConfig.InitialLoadMode.EAGER;

public class MemberDynamicConfig {

    /*
     * Starts a Hazelcast member without any explicit data structure configuration, then adds
     * data structure configuration dynamically
     */
    public static void main(String[] args) {
        // Start a member with no explicit configuration
        HazelcastInstance instance = Hazelcast.newHazelcastInstance();
        ILogger logger = instance.getLoggingService().getLogger(MemberDynamicConfig.class);
        // Obtain a map
        IMap<String, String> defaultMap = instance.getMap("defaultMap");
        // Inspect its config: default is 1 sync backup
        MapConfig defaultMapConfig = instance.getConfig().getMapConfig("defaultMap");
        logger.info("Map \"defaultMapConfig\" has backup count " + defaultMapConfig.getBackupCount());
        logger.info("defaultMap[\"1\"] = " + defaultMap.get("1"));

        // Another application will be using a map with a map loader and no backups
        // Note that wildcards also work for dynamically added data structure configurations
        MapConfig mapWithLoaderConfig = new MapConfig("map-with-loader-*").setBackupCount(0);
        mapWithLoaderConfig.getMapStoreConfig()
                           .setEnabled(true)
                           .setInitialLoadMode(EAGER)
                           .setClassName("EchoMapLoader");

        // add the configuration to the already running member
        instance.getConfig().addMapConfig(mapWithLoaderConfig);

        IMap<String, String> mapWithLoader1 = instance.getMap("map-with-loader-1");
        MapConfig mapWithLoader1Config = instance.getConfig().getMapConfig("map-with-loader-1");
        logger.info("Map \"mapWithLoader1\" has backup count " + mapWithLoader1Config.getBackupCount());
        logger.info("mapWithLoader1[\"1\"] = " + mapWithLoader1.get("1") + " (loaded from configured map loader)");

        instance.shutdown();
    }
}
