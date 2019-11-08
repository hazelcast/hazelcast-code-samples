package com.hazelcast.examples.splitbrain.passthrough;

import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.examples.splitbrain.AbstractCacheSplitBrainSample;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;
import java.util.concurrent.CountDownLatch;

import static com.hazelcast.examples.helper.CommonUtils.assertClusterSizeEventually;
import static com.hazelcast.examples.helper.CommonUtils.assertEquals;
import static com.hazelcast.examples.helper.CommonUtils.assertOpenEventually;
import static com.hazelcast.examples.helper.HazelcastUtils.generateKeyOwnedBy;

/**
 * Base class for jcache split-brain sample based on {@code PASS_THROUGH} cache merge policy.
 *
 * {@code PASS_THROUGH} cache merge policy merges cache entry from source to destination
 * if it does not exist in the destination cache.
 * <p>
 * <b>IMPORTANT</b>: this sample uses internal API {@code HazelcastServerCachingProvider} to
 * start two separate {@code CachingProvider}s and associated {@code CacheManager}s with separate
 * backing {@link HazelcastInstance}s. Application production code should never do that. Instead
 * use JCache standard API methods as described in javadoc of {@link com.hazelcast.cache.HazelcastCachingProvider}
 * or public Hazelcast API.
 * </p>
 */
abstract class AbstractCacheSplitBrainSampleWithPassThroughCacheMergePolicy extends AbstractCacheSplitBrainSample {

    private static final String CACHE_NAME = BASE_CACHE_NAME + "-passthrough";

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

            Cache<String, String> cache1 = getCache(CACHE_NAME, cacheManager1);
            Cache<String, String> cache2 = getCache(CACHE_NAME, cacheManager2);

            String key = generateKeyOwnedBy(h1);
            cache1.put(key, "value");

            cache2.put(key, "passThroughValue");

            assertOpenEventually(splitBrainCompletedLatch);
            assertClusterSizeEventually(2, h1);
            assertClusterSizeEventually(2, h2);

            Cache<String, String> cacheTest = cacheManager2.getCache(CACHE_NAME);
            assertEquals("passThroughValue", cacheTest.get(key));
        } finally {
            Hazelcast.shutdownAll();
        }
    }
}
