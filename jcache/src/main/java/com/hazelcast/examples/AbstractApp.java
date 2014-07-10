package com.hazelcast.examples;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.util.concurrent.TimeUnit;

public class AbstractApp {

    private static Duration TEN_SEC = new Duration(TimeUnit.SECONDS,10);

    CachingProvider cachingProvider;
    CacheManager cacheManager;

    Cache<String, Integer> cache;

    public void initCacheManager() {
        //resolve a cache manager
        cachingProvider = Caching.getCachingProvider();
        cacheManager = cachingProvider.getCacheManager();
    }

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

    public void populateCache() {
        if (cache != null) {
            for (int i = 0; i < 10; i++) {
                cache.put("theKey-" + i, i);
            }
        }
    }

    public void printContent(){
        System.out.println("============================================================");
        for(int i=0;i<10;i++){
            final String key = "theKey-" + i;
            System.out.println("Key: " + key + ", Value: " + cache.get(key));
        }
        System.out.println("============================================================");
    }

    public void shutdown(){
        cacheManager.close();

    }
}
