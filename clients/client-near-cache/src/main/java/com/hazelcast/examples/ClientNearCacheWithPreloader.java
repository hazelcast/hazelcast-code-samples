package com.hazelcast.examples;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.monitor.NearCacheStats;

import java.io.File;

import static com.hazelcast.examples.helper.CommonUtils.sleepMillis;
import static com.hazelcast.nio.IOUtil.deleteQuietly;

public class ClientNearCacheWithPreloader extends NearCacheClientSupport {

    private static final int MAP_SIZE = 10000;

    public static void main(String[] args) {
        File storeFile = new File("articlesPreloader").getAbsoluteFile();
        deleteQuietly(storeFile);
        storeFile.mkdir();

        HazelcastInstance hz = initCluster();
        IMap<Integer, Article> map = hz.getMap("articlesPreloader");

        // populate the map and Near Cache
        for (int i = 0; i < MAP_SIZE; i++) {
            map.put(i, new Article("foo-" + i));
            map.get(i);
        }
        printNearCacheStats(map, "The Near Cache is populated");

        // wait for the persistence of the Near Cache keys
        NearCacheStats stats = map.getLocalMapStats().getNearCacheStats();
        while (stats.getLastPersistenceKeyCount() != MAP_SIZE) {
            sleepMillis(500);
        }
        System.out.println("The Near Cache keys have been persisted: " + stats);

        // shutdown the HZ client
        hz.shutdown();

        // restart the HZ client
        hz = HazelcastClient.newHazelcastClient();

        // trigger the Near Cache pre-loading
        System.out.println("Triggering Near Cache pre-loading");
        map = hz.getMap("articlesPreloader");

        // wait until the pre-loading of the Near Cache is done
        stats = map.getLocalMapStats().getNearCacheStats();
        while (stats.getOwnedEntryCount() != MAP_SIZE) {
            sleepMillis(500);
        }
        printNearCacheStats(map, "The Near Cache has been re-populated with the stored keys");

        // shutdown and cleanup
        shutdown();
        deleteQuietly(storeFile);
    }
}
