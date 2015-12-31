package com.hazelcast.examples.splitbrain.higherhits;

import com.hazelcast.config.Config;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * Declaratively (XML) configured version of `HIGHER_HITS` cache merge policy based jcache split-brain sample.
 */
public class DeclarativeCacheSplitBrainSampleWithHigherHitsCacheMergePolicy
        extends CacheSplitBrainSampleWithHigherHitsCacheMergePolicy {

    @Override
    protected Config getConfig() {
        return newDeclarativeConfig();
    }

    @Override
    protected Cache<String, Object> getCache(String cacheName, CacheManager cacheManager) {
        return cacheManager.getCache(cacheName);
    }

    public static void main(String[] args) {
        new DeclarativeCacheSplitBrainSampleWithHigherHitsCacheMergePolicy().run();
    }
}
