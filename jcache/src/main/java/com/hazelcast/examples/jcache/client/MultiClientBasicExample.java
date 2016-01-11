package com.hazelcast.examples.jcache.client;

import com.hazelcast.examples.AbstractApp;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.expiry.Duration;

public class MultiClientBasicExample extends AbstractApp {

    public static void main(String[] args) throws InterruptedException {
        //ClusterGroup server = new ClusterGroup();
        //server.init();

        new MultiClientBasicExample().runApp();

        //server.shutdown();
    }

    private void runApp() throws InterruptedException {
        // force client be used as a provider
        clientSetup();

        // first thin is we need to initialize the cache Managers for each cluster
        CacheManager cacheManager1 = initCacheManager(uri1);
        CacheManager cacheManager2 = initCacheManager(uri2);

        // create a cache with the provided name
        Cache<String, Integer> cacheAtCluster1 = initCache("theCache", cacheManager1);

        Cache<String, Integer> cacheAtCluster2 = initCache("theCache", cacheManager2, Duration.ETERNAL);

        // populate the content
        populateCache(cacheAtCluster1);
        populateCache(cacheAtCluster2);

        // print the content whatever we have
        printContent(cacheAtCluster1);
        printContent(cacheAtCluster2);

        // wait for 10 sec to expire the content
        sleepFor(10 * 1000);

        // print the content again, and see everything has expired and values are null
        printContent(cacheAtCluster1);
        printContent(cacheAtCluster2);

        // shutdown the cache manager
        shutdown();
    }
}
