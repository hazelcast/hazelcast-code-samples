package com.hazelcast.loader;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.HazelcastInstance;

import static com.hazelcast.core.Hazelcast.newHazelcastInstance;

/**
 * Starter application for read-through / write-through example with Hazelcast and MongoDB.
 * <p>
 * Connection details provided programmatically via {@link MapStoreConfig}
 * Properties includes: connection url, database and collection names
 *
 * @author Viktor Gamov on 11/2/15.
 *         Twitter: @gamussa
 */
public class MemberProgrammatic {
    public static void main(String[] args) {
        Config config = new Config();
        final MapConfig supplementsMapConfig = config.getMapConfig("supplements");

        final MapStoreConfig mapStoreConfig = supplementsMapConfig.getMapStoreConfig();
        mapStoreConfig
                .setEnabled(true)
                .setClassName("com.hazelcast.loader.MongoMapStore")
                .setProperty("mongo.url", "mongodb://localhost:27017")
                .setProperty("mongo.db", "mydb")
                .setProperty("mongo.collection", "supplements");

        final HazelcastInstance hazelcastInstance = newHazelcastInstance(config);
        new ReadWriteThroughCache(hazelcastInstance).run();
    }
}
