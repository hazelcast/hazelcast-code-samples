package com.hazelcast.examples.instance;

import com.hazelcast.cache.ICache;
import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICacheManager;

import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;

/**
 * Demonstrates how to use JCache through `HazelcastInstance` rather than Hazelcast's `CacheManager`.
 */
public class JCacheThroughHazelcastInstanceExample {

    private static final String BASE_CACHE_NAME = "MyCache";

    public static void main(String[] args) {
        Config config = new Config().addCacheConfig(createCacheSimpleConfig(BASE_CACHE_NAME + "_1"));
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        CachingProvider cachingProvider = HazelcastServerCachingProvider.createCachingProvider(instance);
        CacheManager cacheManager = cachingProvider.getCacheManager();

        // ICacheManager is Hazelcast-specific interface, not to be confused with JCache's CacheManager.
        // An instance of the ICacheManager can be obtained from a HazelcastInstance and used to get
        // a reference to an existing cache.
        ICacheManager hazelcastCacheManager = instance.getCacheManager();
        ICache cache1 = hazelcastCacheManager.getCache(BASE_CACHE_NAME + "_1");

        ICache cache2a = (ICache) cacheManager.createCache(BASE_CACHE_NAME + "_2", new CacheConfig(BASE_CACHE_NAME + "_2"));
        ICache cache2b = hazelcastCacheManager.getCache(BASE_CACHE_NAME + "_2");

        System.out.println("cache2a (through CacheManager) == cache2b (through HazelcastInstance): " + (cache2a == cache2b));

        System.out.println("Distributed objects before destroy:");
        for (DistributedObject distributedObject : instance.getDistributedObjects()) {
            System.out.println("\t- Distributed object with name " + distributedObject.getName());
        }

        cache1.destroy();
        cache2a.destroy();

        System.out.println("Distributed objects after destroy:");
        for (DistributedObject distributedObject : instance.getDistributedObjects()) {
            System.out.println("\tDistributed object with name " + distributedObject.getName());
        }

        instance.shutdown();
    }

    private static CacheSimpleConfig createCacheSimpleConfig(String cacheName) {
        return new CacheSimpleConfig().setName(cacheName);
    }
}
