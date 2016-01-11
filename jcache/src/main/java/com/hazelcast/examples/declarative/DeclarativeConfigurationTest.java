package com.hazelcast.examples.declarative;

import com.hazelcast.cache.ICache;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.EvictionConfig;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.spi.CachingProvider;

/**
 * This example shows how to configure cache declaratively
 */
public class DeclarativeConfigurationTest {

    public static void main(String[] args) {
        System.setProperty("hazelcast.config", "classpath:hazelcast-declarative-eviction-test.xml");
        System.setProperty("hazelcast.jcache.provider.type", "server");

        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        // Since we already defined our cache in xml file
        // we don't need to create a cache, one will be created when we call getCache
        ICache<Object, Object> cache = cacheManager.getCache("cache").unwrap(ICache.class);
        CacheConfig cacheConfig = cache.getConfiguration(CacheConfig.class);

        System.out.println("key type: " + cacheConfig.getKeyType());
        System.out.println("value type: " + cacheConfig.getValueType());

        System.out.println("statistics enabled: " + cacheConfig.isStatisticsEnabled());
        System.out.println("management enabled: " + cacheConfig.isManagementEnabled());

        System.out.println("read-through enabled: " + cacheConfig.isReadThrough());
        System.out.println("write-through enabled: " + cacheConfig.isWriteThrough());

        System.out.println("loader factory: " + cacheConfig.getCacheLoaderFactory());
        System.out.println("writer factory: " + cacheConfig.getCacheWriterFactory());
        System.out.println("expiry policy factory: " + cacheConfig.getExpiryPolicyFactory());

        Iterable<CacheEntryListenerConfiguration> cacheEntryListenerConfigurations
                = cacheConfig.getCacheEntryListenerConfigurations();
        for (CacheEntryListenerConfiguration cacheEntryListenerConfiguration : cacheEntryListenerConfigurations) {
            System.out.println("listener factory: " + cacheEntryListenerConfiguration.getCacheEntryListenerFactory());
        }

        System.out.println("in-memory-format: " + cacheConfig.getInMemoryFormat());

        System.out.println("backup count: " + cacheConfig.getBackupCount());
        System.out.println("async backup count: " + cacheConfig.getAsyncBackupCount());

        EvictionConfig evictionConfig = cacheConfig.getEvictionConfig();
        System.out.println("max-size: " + evictionConfig.getSize());
        System.out.println("max-size-policy: " + evictionConfig.getMaximumSizePolicy());
        System.out.println("eviction-policy: " + evictionConfig.getEvictionPolicy());

        System.exit(0);
    }
}
