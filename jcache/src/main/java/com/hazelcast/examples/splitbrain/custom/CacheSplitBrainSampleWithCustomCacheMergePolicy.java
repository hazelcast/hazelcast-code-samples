package com.hazelcast.examples.splitbrain.custom;

import com.hazelcast.cache.CacheEntryView;
import com.hazelcast.cache.CacheMergePolicy;
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
 * Base class for jcache split-brain sample based on `custom cache merge policy.
 * </p>
 *
 * <p>
 * Custom cache merge policy implements {@link com.hazelcast.cache.CacheMergePolicy} and handles its own logic.
 * </p>
 */
abstract class CacheSplitBrainSampleWithCustomCacheMergePolicy extends AbstractCacheSplitBrainSample {

    private static final String CACHE_NAME = BASE_CACHE_NAME + "-custom";

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

            Cache<String, Object> cache1 = getCache(CACHE_NAME, cacheManager1);
            Cache<String, Object> cache2 = getCache(CACHE_NAME, cacheManager2);

            // TODO We assume that until here and also while doing get/put, cluster is still split
            // this assumptions seems fragile due to time sensitivity

            String key = generateKeyOwnedBy(h1);
            cache1.put(key, "value");

            cache2.put(key, Integer.valueOf(1));

            assertOpenEventually(splitBrainCompletedLatch);
            assertClusterSizeEventually(2, h1);
            assertClusterSizeEventually(2, h2);

            Cache<String, Object> cacheTest = cacheManager2.getCache(CACHE_NAME);
            Object value = cacheTest.get(key);
            assertTrue("Value with key `" + key + "` should be there!", value != null);
            assertTrue("Value with key `" + key + "` should be Integer!", value instanceof Integer);
        } finally {
            Hazelcast.shutdownAll();
        }
    }

    public static class CustomCacheMergePolicy implements CacheMergePolicy {

        @Override
        public Object merge(String cacheName, CacheEntryView mergingEntry, CacheEntryView existingEntry) {
            if (mergingEntry.getValue() instanceof Integer) {
                return mergingEntry.getValue();
            }
            return null;
        }
    }
}
