package com.hazelcast.examples.splitbrain.putifabsent;

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
 * Base class for jcache split-brain sample based on `PUT_IF_ABSENT` cache merge policy.
 * </p>
 *
 * <p>
 * `PASS_THROUGH` cache merge policy merges cache entry from source to destination directly.
 * </p>
 */
abstract class CacheSplitBrainSampleWithPutIfAbsentCacheMergePolicy extends AbstractCacheSplitBrainSample {

    private static final String CACHE_NAME = BASE_CACHE_NAME + "-putifabsent";

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

            cache1.put("key1", "PutIfAbsentValue1");

            cache2.put("key1", "value");
            cache2.put("key2", "PutIfAbsentValue2");

            assertOpenEventually(splitBrainCompletedLatch);
            assertClusterSizeEventually(2, h1);
            assertClusterSizeEventually(2, h2);

            Cache<String, String> cacheTest = cacheManager2.getCache(CACHE_NAME);
            assertEquals("PutIfAbsentValue1", cacheTest.get("key1"));
            assertEquals("PutIfAbsentValue2", cacheTest.get("key2"));
        } finally {
            Hazelcast.shutdownAll();
        }
    }
}
