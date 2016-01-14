package com.hazelcast.examples.splitbrain.custom;

import com.hazelcast.config.Config;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * Declaratively (XML) configured version of custom cache merge policy based jcache split-brain sample.
 */
public class DeclarativeCacheSplitBrainSampleWithCustomCacheMergePolicy
        extends CacheSplitBrainSampleWithCustomCacheMergePolicy {

    @Override
    protected Config getConfig() {
        return newDeclarativeConfig();
    }

    @Override
    protected Cache<String, Object> getCache(String cacheName, CacheManager cacheManager) {
        return cacheManager.getCache(cacheName);
    }

    public static void main(String[] args) {
        new DeclarativeCacheSplitBrainSampleWithCustomCacheMergePolicy().run();
    }
}
