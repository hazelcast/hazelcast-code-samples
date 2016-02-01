package com.hazelcast.examples.splitbrain.latestaccess;

import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.examples.splitbrain.AbstractCacheSplitBrainSample;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;
import java.util.concurrent.CountDownLatch;

/**
 * <p>
 * Base class for jcache split-brain sample based on `LATEST_ACCESS` cache merge policy.
 * </p>
 *
 * <p>
 * `LATEST_ACCESS` cache merge policy merges cache entry from source to destination cache
 * if source entry has been accessed more recently than the destination entry.
 * </p>
 */
abstract class CacheSplitBrainSampleWithLatestAccessCacheMergePolicy extends AbstractCacheSplitBrainSample {

    private static final String CACHE_NAME = BASE_CACHE_NAME + "-latestaccess";

    protected abstract Config getConfig();

    protected abstract Cache getCache(String cacheName, CacheManager cacheManager);

    protected void run() {
        try {
            Config config = getConfig();
            HazelcastInstance h1 = Hazelcast.newHazelcastInstance(config);
            HazelcastInstance h2 = Hazelcast.newHazelcastInstance(config);

            CountDownLatch splitBrainCompletedLatch = simulateSplitBrain(h1, h2);

            CachingProvider cachingProvider1 = HazelcastServerCachingProvider.createCachingProvider(h1);
            CachingProvider cachingProvider2 = HazelcastServerCachingProvider.createCachingProvider(h2);

            CacheManager cacheManager1 = cachingProvider1.getCacheManager();
            CacheManager cacheManager2 = cachingProvider2.getCacheManager();

            Cache<String, String> cache1 = getCache(CACHE_NAME, cacheManager1);
            Cache<String, String> cache2 = getCache(CACHE_NAME, cacheManager2);

            // TODO We assume that until here and also while doing get/put, cluster is still split
            // this assumptions seems fragile due to time sensitivity

            cache1.put("key1", "value");
            // access to record
            assertEquals("value", cache1.get("key1"));

            // prevent updating at the same time
            sleepAtLeastMillis(1);

            cache2.put("key1", "LatestUpdatedValue");
            // access to record
            assertEquals("LatestUpdatedValue", cache2.get("key1"));

            cache2.put("key2", "value2");
            // access to record
            assertEquals("value2", cache2.get("key2"));

            // Prevent updating at the same time
            sleepAtLeastMillis(1);

            cache1.put("key2", "LatestUpdatedValue2");
            // access to record
            assertEquals("LatestUpdatedValue2", cache1.get("key2"));

            assertOpenEventually(splitBrainCompletedLatch);
            assertClusterSizeEventually(2, h1);
            assertClusterSizeEventually(2, h2);

            Cache<String, String> cacheTest = cacheManager1.getCache(CACHE_NAME);
            assertEquals("LatestUpdatedValue", cacheTest.get("key1"));
            assertEquals("LatestUpdatedValue2", cacheTest.get("key2"));
        } finally {
            Hazelcast.shutdownAll();
        }
    }
}
