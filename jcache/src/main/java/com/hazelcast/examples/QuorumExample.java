package com.hazelcast.examples;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.net.URISyntaxException;

public class QuorumExample {

    private static final String CACHE_NAME = "cache-with-quorum";

    public static void main(String[] args) throws URISyntaxException {
        System.setProperty("hazelcast.jcache.provider.type", "server");
        System.setProperty("hazelcast.config", "classpath:hazelcast-quorum.xml");

        // this creates a cluster with 1 node
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance();

        // this creates a new node and joins the cluster creating a cluster with 2 nodes
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();

        // Quorum will succeed
        Cache<String, String> cache = cacheManager.getCache(CACHE_NAME);
        cache.put("key", "we have the quorum");

        System.out.println("Quorum is satisfied, so the following put will throw no exception");

        System.out.println("Shutdown one instance, so there won't be enough members for quorum presence");
        instance1.getLifecycleService().shutdown();

        // Quorum will fail
        System.out.println("The following put operation will fail");
        try {
            cache.put("key2", "will not succeed");
        } catch (Exception expected) {
            System.out.println("Put operation failed with expected QuorumException: " + expected.getMessage());
        }
        Hazelcast.shutdownAll();
    }
}
