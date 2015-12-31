package com.hazelcast.examples.splitbrain.passthrough;

import com.hazelcast.cache.merge.PassThroughCacheMergePolicy;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * Programmatically configured version of `PASS_THROUGH` cache merge policy based jcache split-brain sample.
 */
public class ProgrammaticCacheSplitBrainSampleWithPassThroughCacheMergePolicy
        extends CacheSplitBrainSampleWithPassThroughCacheMergePolicy {

    @Override
    protected Config getConfig() {
        return newProgrammaticConfig();
    }

    @Override
    protected Cache<String, Object> getCache(String cacheName, CacheManager cacheManager) {
        CacheConfig<String, Object> cacheConfig = newCacheConfig(cacheName, PassThroughCacheMergePolicy.class.getName());
        return cacheManager.createCache(cacheName, cacheConfig);
    }

    public static void main(String[] args) {
        new ProgrammaticCacheSplitBrainSampleWithPassThroughCacheMergePolicy().run();
    }
}
