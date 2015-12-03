package com.hazelcast.examples.splitbrain.putifabsent;

import com.hazelcast.cache.merge.PutIfAbsentCacheMergePolicy;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * Programmatically configured version of `PUT_IF_ABSENT` cache merge policy based jcache split-brain sample.
 */
public class ProgrammaticCacheSplitBrainSampleWithPutIfAbsentCacheMergePolicy
        extends CacheSplitBrainSampleWithPutIfAbsentCacheMergePolicy {

    @Override
    protected Config getConfig() {
        return newProgrammaticConfig();
    }

    @Override
    protected Cache<String, Object> getCache(String cacheName, CacheManager cacheManager) {
        CacheConfig<String, Object> cacheConfig = newCacheConfig(cacheName, PutIfAbsentCacheMergePolicy.class.getName());
        return cacheManager.createCache(cacheName, cacheConfig);
    }

    public static void main(String[] args) {
        new ProgrammaticCacheSplitBrainSampleWithPutIfAbsentCacheMergePolicy().run();
    }
}
