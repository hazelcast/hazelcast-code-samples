package com.hazelcast.examples.declarative;

import com.hazelcast.cache.ICache;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.EvictionConfig;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

/**
 * This example shows how to configure eviction for cache declaratively
 */
public class EvictionTest {

    public static void main(String[] args) {
        System.setProperty("hazelcast.config", "classpath:hazelcast-declarative-eviction-test.xml");
        System.setProperty("hazelcast.jcache.provider.type", "server");

        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        // since we already defined our cache in XML file,
        // we don't need to create a cache, one will be created when we call getCache
        ICache<Object, Object> cache = cacheManager.getCache("cache").unwrap(ICache.class);
        CacheConfig cacheConfig = cache.getConfiguration(CacheConfig.class);

        EvictionConfig evictionConfig = cacheConfig.getEvictionConfig();
        System.out.println("cache configured with eviction -> size:" + evictionConfig.getSize()
                + " policy:" + evictionConfig.getEvictionPolicyType());


        // initial put [0 to 50.000]
        for (int i = 0; i < 50000; i++) {
            cache.put(i, i);
        }

        // to mark these entries [0 to 10.000] as frequently used
        for (int i = 0; i < 10000; i++) {
            cache.get(i);
            cache.get(i);
            cache.get(i);
        }

        // put more [50.000 to 150.000]
        for (int i = 50000; i < 150000; i++) {
            cache.put(i, i);
        }

        // check the size, it should be around 100.000 as stated in the config xml
        System.out.println("cache size: " + cache.size());

        // check if any of out frequently used entries get evicted
        for (int i = 0; i < 10000; i++) {
            if (!cache.containsKey(i)) {
                System.out.println("cache does not contains key: " + i);
            }
        }
        System.exit(0);
    }
}
