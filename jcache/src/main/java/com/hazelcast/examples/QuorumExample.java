package com.hazelcast.examples;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.net.URISyntaxException;

public class QuorumExample {

    private static final String CACHE_NAME = "cache-with-quorum";

    public static void main(String[] args)
            throws URISyntaxException {
        System.setProperty("hazelcast.jcache.provider.type", "server");
        System.setProperty("hazelcast.config", "classpath:hazelcast-quorum.xml");

        //this creates a cluster with 1 node
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance();

        //this creates a new node and joins the cluster creating a cluster with 2 nodes
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();

        Cache<String, String> cache = cacheManager.getCache(CACHE_NAME);
        cache.put("key", "value");

        System.out.println("Quorum is satisfied and key/value put into the cache without problem");

        System.out.println("Now killing one instance, and there won't be enough members for quorum presence");
        instance1.getLifecycleService().shutdown();

        System.out.println("Following put operation will fail");
        try {
            cache.put("key2", "value2");
        } catch (Exception e) {
            System.out.println("Put operation failed with exception -> " + e.getMessage());
        }
        Hazelcast.shutdownAll();
    }
}
