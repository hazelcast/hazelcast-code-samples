package com.hazelcast.examples.declarative;

import com.hazelcast.cache.HazelcastExpiryPolicy;
import com.hazelcast.cache.ICache;
import com.hazelcast.config.CacheConfig;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.spi.CachingProvider;

/**
 * This example shows how to configure listener for cache declaratively
 */
public class EntryListenerTest {

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("hazelcast.config", "classpath:hazelcast-declarative-listener-test.xml");
        System.setProperty("hazelcast.jcache.provider.type", "server");

        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        // since we already defined our cache in XML file,
        // we don't need to create a cache, one will be created when we call getCache
        ICache<Object, Object> cache = cacheManager.getCache("cache").unwrap(ICache.class);
        CacheConfig cacheConfig = cache.getConfiguration(CacheConfig.class);
        Iterable<CacheEntryListenerConfiguration> cacheEntryListenerConfigurations
                = cacheConfig.getCacheEntryListenerConfigurations();
        for (CacheEntryListenerConfiguration cacheEntryListenerConfiguration : cacheEntryListenerConfigurations) {
            System.out.println("cache configured with listener -> "
                    + cacheEntryListenerConfiguration.getCacheEntryListenerFactory());
        }

        // entry create
        cache.put("key", "value");

        // entry update
        cache.put("key", "value1");

        // entry remove
        cache.remove("key");

        // entry will expire after 1 sec
        cache.put("key", "value2", new HazelcastExpiryPolicy(1000, Long.MAX_VALUE, Long.MAX_VALUE));
        Thread.sleep(2000);
        System.out.println("value should be null: " + cache.get("key"));

        System.exit(0);
    }
}
