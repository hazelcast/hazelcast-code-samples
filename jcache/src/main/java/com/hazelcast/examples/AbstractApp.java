package com.hazelcast.examples;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.util.concurrent.TimeUnit;


/**
 * Base application
 */
public class AbstractApp {

    private static Duration TEN_SEC = new Duration(TimeUnit.SECONDS,10);

    CachingProvider cachingProvider;
    CacheManager cacheManager;

    Cache<String, Integer> cache;

    /**
     * initialize the JCache Manager that we will use for creating and getting a cache object
     */
    public void initCacheManager() {
        //resolve a cache manager
        cachingProvider = Caching.getCachingProvider();
        cacheManager = cachingProvider.getCacheManager();
    }

    /**
     * we initialize a cache with name
     * @param name
     */
    public void initCache(String name) {

        //configure the cache
        MutableConfiguration<String, Integer> config = new MutableConfiguration<String, Integer>();
        config.setStoreByValue(true)
                .setTypes(String.class, Integer.class)
                .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(TEN_SEC))
                .setStatisticsEnabled(false);

        //create the cache
        cacheManager.createCache(name, config);


        //get the cache
        cache = cacheManager.getCache(name, String.class, Integer.class);
    }

    /**
     * we populate cache with (theKey-i, i )
     */
    public void populateCache() {
        if (cache != null) {
            for (int i = 0; i < 10; i++) {
                cache.put("theKey-" + i, i);
            }
        }
    }

    /**
     * print all of the content of the cache, if expires or not exist you will see a null value
     */
    public void printContent(){
        System.out.println("============================================================");
        for(int i=0;i<10;i++){
            final String key = "theKey-" + i;
            System.out.println("Key: " + key + ", Value: " + cache.get(key));
        }
        System.out.println("============================================================");
    }

    /**
     * closing the cache manager we started
     */
    public void shutdown(){
        cacheManager.close();

    }
}
