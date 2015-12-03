package com.hazelcast.examples.splitbrain.higherhits;

import com.hazelcast.cache.merge.HigherHitsCacheMergePolicy;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * Programmatically configured version of `HIGHER_HITS` cache merge policy based jcache split-brain sample.
 */
public class ProgrammaticCacheSplitBrainSampleWithHigherHitsCacheMergePolicy
        extends CacheSplitBrainSampleWithHigherHitsCacheMergePolicy {

    @Override
    protected Config getConfig() {
        return newProgrammaticConfig();
    }

    @Override
    protected Cache<String, Object> getCache(String cacheName, CacheManager cacheManager) {
        CacheConfig<String, Object> cacheConfig = newCacheConfig(cacheName, HigherHitsCacheMergePolicy.class.getName());
        return cacheManager.createCache(cacheName, cacheConfig);
    }

    public static void main(String[] args) {
        new ProgrammaticCacheSplitBrainSampleWithHigherHitsCacheMergePolicy().run();
    }
}
