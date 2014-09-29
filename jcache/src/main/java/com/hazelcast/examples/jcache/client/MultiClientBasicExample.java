package com.hazelcast.examples.jcache.client;

import com.hazelcast.examples.AbstractApp;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.expiry.Duration;

/**
 * Created by asim on 29.09.2014.
 */
public class MultiClientBasicExample
        extends AbstractApp{


    public static void main(String[] args) throws InterruptedException {
//        ClusterGroup server = new ClusterGroup();
//        server.init();

        new MultiClientBasicExample().runApp();

//        server.shutdown();
    }

    public void runApp()
            throws InterruptedException {

        //Force client be used as a provider
        clientSetup();

        //first thin is we need to initialize the cache Managers for each cluster
        final CacheManager cacheManager1 = initCacheManager(uri1);
        final CacheManager cacheManager2 = initCacheManager(uri2);

        //create a cache with the provided name
        final Cache<String, Integer> cacheAtCluster1 = initCache("theCache", cacheManager1);

        final Cache<String, Integer> cacheAtCluster2 = initCache("theCache", cacheManager2, Duration.ETERNAL);

        //lets populate the content
        populateCache(cacheAtCluster1);
        populateCache(cacheAtCluster2);

        //so we print the content whatever we have
        printContent(cacheAtCluster1);
        printContent(cacheAtCluster2);
        //lets wait for 10 sec to expire the content
        sleepFor(10 * 1000);

        //and print the content again, and see everything has expired and values are null
        printContent(cacheAtCluster1);
        printContent(cacheAtCluster2);

        //lastly shutdown the cache manager
        shutdown();
    }


}
