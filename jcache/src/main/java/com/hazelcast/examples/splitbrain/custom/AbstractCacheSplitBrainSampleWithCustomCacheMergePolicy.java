package com.hazelcast.examples.splitbrain.custom;

import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.examples.splitbrain.AbstractCacheSplitBrainSample;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.merge.MergingValue;
import com.hazelcast.spi.merge.SplitBrainMergePolicy;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static com.hazelcast.examples.helper.CommonUtils.assertClusterSizeEventually;
import static com.hazelcast.examples.helper.CommonUtils.assertOpenEventually;
import static com.hazelcast.examples.helper.CommonUtils.assertTrue;
import static com.hazelcast.examples.helper.HazelcastUtils.generateKeyOwnedBy;

/**
 * Base class for jcache split-brain sample based on custom cache merge policy.
 *
 * Custom cache merge policy implements {@link com.hazelcast.spi.merge.SplitBrainMergePolicy} and handles its own logic.
 * <p>
 * <b>IMPORTANT</b>: this sample uses internal API {@code HazelcastServerCachingProvider} to
 * start two separate {@code CachingProvider}s and associated {@code CacheManager}s with separate
 * backing {@link HazelcastInstance}s. Application production code should never do that. Instead
 * use JCache standard API methods as described in javadoc of {@link com.hazelcast.cache.HazelcastCachingProvider}
 * or public Hazelcast API.
 * </p>
 */
abstract class AbstractCacheSplitBrainSampleWithCustomCacheMergePolicy extends AbstractCacheSplitBrainSample {

    private static final String CACHE_NAME = BASE_CACHE_NAME + "-custom";

    protected abstract Config getConfig();

    protected abstract Cache getCache(String cacheName, CacheManager cacheManager);

    protected void run() {
        try {
            Config config = getConfig();
            HazelcastInstance h1 = Hazelcast.newHazelcastInstance(config);
            HazelcastInstance h2 = Hazelcast.newHazelcastInstance(config);

            CountDownLatch splitBrainCompletedLatch = simulateSplitBrain(h1, h2);

            CachingProvider cachingProvider1 = new HazelcastServerCachingProvider(h1);
            CachingProvider cachingProvider2 = new HazelcastServerCachingProvider(h2);

            CacheManager cacheManager1 = cachingProvider1.getCacheManager();
            CacheManager cacheManager2 = cachingProvider2.getCacheManager();

            Cache<String, Object> cache1 = getCache(CACHE_NAME, cacheManager1);
            Cache<String, Object> cache2 = getCache(CACHE_NAME, cacheManager2);

            // TODO: we assume that until here and also while doing get/put, cluster is still split
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
            System.out.println("============ " + value);
            assertTrue("Value with key `" + key + "` should be Integer!", value instanceof Integer);
        } finally {
            Hazelcast.shutdownAll();
        }
    }

    public static class CustomCacheMergePolicy implements SplitBrainMergePolicy<Object, MergingValue<Object>> {

        @Override
        public Object merge(MergingValue<Object> mergingValue, MergingValue<Object> existingValue) {
            if (mergingValue.getValue() instanceof Integer) {
                return mergingValue.getValue();
            }
            return null;
        }

        @Override
        public void writeData(ObjectDataOutput objectDataOutput) throws IOException {

        }

        @Override
        public void readData(ObjectDataInput objectDataInput) throws IOException {

        }
    }
}
