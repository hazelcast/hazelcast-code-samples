package com.hazelcast.examples.splitbrain.putifabsent;

import com.hazelcast.config.Config;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * Declaratively (XML) configured version of `PUT_IF_ABSENT` cache merge policy based jcache split-brain sample.
 */
public class DeclarativeCacheSplitBrainSampleWithPutIfAbsentCacheMergePolicy
        extends CacheSplitBrainSampleWithPutIfAbsentCacheMergePolicy {

    @Override
    protected Config getConfig() {
        return newDeclarativeConfig();
    }

    @Override
    protected Cache<String, Object> getCache(String cacheName, CacheManager cacheManager) {
        return cacheManager.getCache(cacheName);
    }

    public static void main(String[] args) {
        new DeclarativeCacheSplitBrainSampleWithPutIfAbsentCacheMergePolicy().run();
    }
}
