package com.hazelcast.examples;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.monitor.NearCacheStats;

import static com.hazelcast.core.Hazelcast.newHazelcastInstance;
import static com.hazelcast.examples.helper.CommonUtils.sleepSeconds;
import static com.hazelcast.spi.properties.GroupProperty.CACHE_INVALIDATION_MESSAGE_BATCH_FREQUENCY_SECONDS;
import static java.lang.Integer.parseInt;

abstract class NearCacheSupport {

    protected static HazelcastInstance serverInstance;

    private static final int INVALIDATION_DELAY_SECONDS
            = 2 * parseInt(CACHE_INVALIDATION_MESSAGE_BATCH_FREQUENCY_SECONDS.getDefaultValue());

    protected static HazelcastInstance initCluster() {
        serverInstance = Hazelcast.newHazelcastInstance();
        return serverInstance;
    }

    protected static HazelcastInstance[] initCluster(int clusterSize) {
        HazelcastInstance[] instances = new HazelcastInstance[clusterSize];
        for (int i = 0; i < clusterSize; i++) {
            instances[i] = newHazelcastInstance();
            serverInstance = instances[i];
        }
        return instances;
    }

    protected static void shutdown() {
        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }

    protected static void printNearCacheStats(IMap<?, Article> map) {
        NearCacheStats stats = map.getLocalMapStats().getNearCacheStats();

        System.out.printf("The Near Cache contains %d entries.%n", stats.getOwnedEntryCount());
        System.out.printf("The first article instance was retrieved from the remote instance (Near Cache misses: %d).%n",
                stats.getMisses());
        System.out.printf(
                "The second and third article instance were retrieved from the local Near Cache (Near Cache hits: %d).%n",
                stats.getHits());
    }

    protected static void printNearCacheStats(IMap<?, Article> map, String message) {
        NearCacheStats stats = map.getLocalMapStats().getNearCacheStats();
        System.out.printf("%s (%d entries, %d hits, %d misses, %d evictions, %d expirations)%n",
                message, stats.getOwnedEntryCount(), stats.getHits(), stats.getMisses(),
                stats.getEvictions(), stats.getExpirations());
    }

    @SuppressWarnings("SameParameterValue")
    protected static void waitForNearCacheEvictionCount(IMap<?, Article> map, int expectedEvictionCount) {
        long evictionCount;
        do {
            NearCacheStats stats = map.getLocalMapStats().getNearCacheStats();
            evictionCount = stats.getEvictions();
        } while (evictionCount > expectedEvictionCount);
    }

    protected static void waitForInvalidationEvents() {
        sleepSeconds(INVALIDATION_DELAY_SECONDS);
    }
}
