package com.hazelcast.examples.splitbrain.higherhits;

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
 * Base class for jcache split-brain sample based on `HIGHER_HITS` cache merge policy.
 * </p>
 *
 * <p>
 * `HIGHER_HITS` cache merge policy merges cache entry from source to destination cache
 * if source entry has more hits than the destination one.
 * </p>
 */
abstract class CacheSplitBrainSampleWithHigherHitsCacheMergePolicy extends AbstractCacheSplitBrainSample {

    private static final String CACHE_NAME = BASE_CACHE_NAME + "-higherhits";

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

            cache1.put("key1", "higherHitsValue");
            cache1.put("key2", "value2");

            // Increase hits number
            assertEquals("higherHitsValue", cache1.get("key1"));
            assertEquals("higherHitsValue", cache1.get("key1"));

            cache2.put("key1", "value1");
            cache2.put("key2", "higherHitsValue2");

            // Increase hits number
            assertEquals("higherHitsValue2", cache2.get("key2"));
            assertEquals("higherHitsValue2", cache2.get("key2"));

            assertOpenEventually(splitBrainCompletedLatch);
            assertClusterSizeEventually(2, h1);
            assertClusterSizeEventually(2, h2);

            Cache<String, String> cacheTest = cacheManager2.getCache(CACHE_NAME);
            assertEquals("higherHitsValue", cacheTest.get("key1"));
            assertEquals("higherHitsValue2", cacheTest.get("key2"));
        } finally {
            Hazelcast.shutdownAll();
        }
    }

}
