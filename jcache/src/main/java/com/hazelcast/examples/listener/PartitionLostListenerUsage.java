package com.hazelcast.examples.listener;

import com.hazelcast.cache.ICache;
import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.util.concurrent.CountDownLatch;

import static com.hazelcast.cache.HazelcastCachingProvider.propertiesByInstanceItself;

public class PartitionLostListenerUsage {

    public static void main(String[] args) throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);

        String cacheName1 = "myCache1";
        String cacheName2 = "myCache2";

        HazelcastInstance serverInstance1 = Hazelcast.newHazelcastInstance(new Config());
        HazelcastInstance serverInstance2 = Hazelcast.newHazelcastInstance(new Config());

        CachingProvider cachingProvider = Caching.getCachingProvider(HazelcastServerCachingProvider.class.getName());
        CacheManager cacheManager = cachingProvider.getCacheManager(null, null,
                propertiesByInstanceItself(serverInstance1));

        CacheConfig<Integer, String> config1 = new CacheConfig<>();
        // might lose data if any node crashes
        config1.setBackupCount(0);
        Cache<Integer, String> cache1 = cacheManager.createCache(cacheName1, config1);

        cache1.put(1, "Berlin");

        ICache iCache1 = cache1.unwrap(ICache.class);
        iCache1.addPartitionLostListener(cachePartitionLostEvent -> {
            System.out.println(cachePartitionLostEvent);
            latch.countDown();
        });

        CacheConfig<Integer, String> config2 = new CacheConfig<>();
        // keeps its data if a single node crashes
        config2.setBackupCount(1);
        Cache<Integer, String> cache2 = cacheManager.createCache(cacheName2, config2);

        cache1.put(1, "Berlin");

        ICache iCache2 = cache2.unwrap(ICache.class);
        iCache2.addPartitionLostListener(
                cachePartitionLostEvent -> System.err.println("This line should not be printed! " + cachePartitionLostEvent));

        System.out.println("Terminating second Hazelcast instance...");
        serverInstance2.getLifecycleService().terminate();

        // wait for a least one CachePartitionLostEvent
        latch.await();
        Hazelcast.shutdownAll();
    }
}
