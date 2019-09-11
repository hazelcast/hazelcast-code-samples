package com.hazelcast.examples;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.net.URISyntaxException;

public class SplitBrainProtectionExample {

    private static final String CACHE_NAME = "cache-with-split-brain-protection";

    public static void main(String[] args) throws URISyntaxException {
        System.setProperty("hazelcast.jcache.provider.type", "server");
        System.setProperty("hazelcast.config", "classpath:hazelcast-split-brain-protection.xml");

        // this creates a cluster with 1 node
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance();

        // this creates a new node and joins the cluster creating a cluster with 2 nodes
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();

        // Split brain protection will succeed
        Cache<String, String> cache = cacheManager.getCache(CACHE_NAME);
        cache.put("key", "we have the split brain protection");

        System.out.println("Split brain protection is satisfied, so the following put will throw no exception");

        System.out.println("Shutdown one instance, so there won't be enough members for split brain protection presence");
        instance1.getLifecycleService().shutdown();

        // Split brain protection will fail
        System.out.println("The following put operation will fail");
        try {
            cache.put("key2", "will not succeed");
        } catch (Exception expected) {
            System.out.println("Put operation failed with expected SplitBrainProtectionException: " + expected.getMessage());
        }
        Hazelcast.shutdownAll();
    }
}
