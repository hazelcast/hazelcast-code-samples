package com.hazelcast.examples.splitbrain.passthrough;

import com.hazelcast.config.Config;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * Declaratively (XML) configured version of `PASS_THROUGH` cache merge policy based jcache split-brain sample.
 */
public class DeclarativeCacheSplitBrainSampleWithPassThroughCacheMergePolicy
        extends CacheSplitBrainSampleWithPassThroughCacheMergePolicy {

    @Override
    protected Config getConfig() {
        return newDeclarativeConfig();
    }

    @Override
    protected Cache<String, Object> getCache(String cacheName, CacheManager cacheManager) {
        return cacheManager.getCache(cacheName);
    }

    public static void main(String[] args) {
        new DeclarativeCacheSplitBrainSampleWithPassThroughCacheMergePolicy().run();
    }
}
