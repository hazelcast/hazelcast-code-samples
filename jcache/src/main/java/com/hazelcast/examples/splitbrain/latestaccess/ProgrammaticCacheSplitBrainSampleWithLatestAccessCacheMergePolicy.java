package com.hazelcast.examples.splitbrain.latestaccess;

import com.hazelcast.cache.merge.LatestAccessCacheMergePolicy;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * Programmatically configured version of `LATEST_ACCESS` cache merge policy based jcache split-brain sample.
 */
public class ProgrammaticCacheSplitBrainSampleWithLatestAccessCacheMergePolicy
        extends CacheSplitBrainSampleWithLatestAccessCacheMergePolicy {

    @Override
    protected Config getConfig() {
        return newProgrammaticConfig();
    }

    @Override
    protected Cache<String, Object> getCache(String cacheName, CacheManager cacheManager) {
        CacheConfig<String, Object> cacheConfig = newCacheConfig(cacheName, LatestAccessCacheMergePolicy.class.getName());
        return cacheManager.createCache(cacheName, cacheConfig);
    }

    public static void main(String[] args) {
        new ProgrammaticCacheSplitBrainSampleWithLatestAccessCacheMergePolicy().run();
    }
}
