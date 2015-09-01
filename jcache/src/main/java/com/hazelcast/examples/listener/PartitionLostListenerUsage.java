package com.hazelcast.examples.listener;


import com.hazelcast.cache.ICache;
import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.cache.impl.event.CachePartitionLostEvent;
import com.hazelcast.cache.impl.event.CachePartitionLostListener;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import javax.cache.Cache;
import javax.cache.CacheManager;

import static com.hazelcast.cache.impl.HazelcastServerCachingProvider.createCachingProvider;

public class PartitionLostListenerUsage {

    public static void main(String[] args) {

        String cacheName1 = "myCache1";
        String cacheName2 = "myCache2";

        HazelcastInstance serverInstance1  = Hazelcast.newHazelcastInstance(new Config());
        HazelcastInstance serverInstance2  = Hazelcast.newHazelcastInstance(new Config());

        final HazelcastServerCachingProvider cachingProvider = createCachingProvider(serverInstance1);
        final CacheManager cacheManager = cachingProvider.getCacheManager();

        final CacheConfig<Integer, String> config1 = new CacheConfig<Integer, String>();
        config1.setBackupCount(0); //might lose data if any node crashes
        final Cache<Integer, String> cache1 = cacheManager.createCache(cacheName1, config1);
        cache1.put(1, "Berlin");
        final ICache iCache1 = cache1.unwrap(ICache.class);

        iCache1.addPartitionLostListener(new CachePartitionLostListener() {
            @Override
            public void partitionLost(CachePartitionLostEvent cachePartitionLostEvent) {
                System.out.println(cachePartitionLostEvent);
            }
        });

        final CacheConfig<Integer, String> config2 = new CacheConfig<Integer, String>();
        config2.setBackupCount(1); //keeps its data if a single node crashes
        final Cache<Integer, String> cache2 = cacheManager.createCache(cacheName2, config2);
        cache1.put(1, "Berlin");
        final ICache iCache2 = cache2.unwrap(ICache.class);

        iCache2.addPartitionLostListener(new CachePartitionLostListener() {
            @Override
            public void partitionLost(CachePartitionLostEvent cachePartitionLostEvent) {
                System.out.println("This line will not be printed! " + cachePartitionLostEvent);
            }
        });

        serverInstance2.getLifecycleService().terminate();
    }
}
