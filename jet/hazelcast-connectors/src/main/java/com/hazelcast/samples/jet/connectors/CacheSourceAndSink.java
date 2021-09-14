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

package com.hazelcast.samples.jet.connectors;

import com.hazelcast.cache.ICache;
import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;

/**
 * Demonstrates the usage of Hazelcast ICache as source and sink
 * with the Pipeline API. This will take the contents of one cache
 * and write it into another cache.
 */
public class CacheSourceAndSink {

    private static final int ITEM_COUNT = 10;
    private static final String SOURCE_NAME = "source";
    private static final String SINK_NAME = "sink";

    public static void main(String[] args) {
        Config config = new Config();

        // Unlike with IMap, ICache names must be explicitly configured before using
        config.addCacheConfig(new CacheSimpleConfig().setName(SOURCE_NAME))
              .addCacheConfig(new CacheSimpleConfig().setName(SINK_NAME));

        config.getJetConfig().setEnabled(true);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        JetService jet = hz.getJet();

        try {
            ICache<Integer, Integer> sourceCache = hz.getCacheManager().getCache(SOURCE_NAME);
            for (int i = 0; i < ITEM_COUNT; i++) {
                sourceCache.put(i, i);
            }

            Pipeline p = Pipeline.create();
            p.readFrom(Sources.cache(SOURCE_NAME))
             .writeTo(Sinks.cache(SINK_NAME));

            jet.newJob(p).join();

            ICache<Integer, Integer> sinkCache = hz.getCacheManager().getCache(SINK_NAME);
            System.out.println("Sink cache entries: ");
            sinkCache.forEach(e -> System.out.println(e.getKey() + "=" + e.getValue()));
        } finally {
            Hazelcast.shutdownAll();
        }
    }
}
