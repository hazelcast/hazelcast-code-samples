package com.hazelcast.examples.jcache.client;

import com.hazelcast.examples.AbstractApp;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * Simple Client cache example
 */
public class SingleClientBasicExample extends AbstractApp{

    public static void main(String[] args) throws InterruptedException {
        new SingleClientBasicExample().runApp();
    }

    public void runApp()
            throws InterruptedException {

        //Force client be used as a provider
        clientSetup();

        //first thin is we need to initialize the cache Manager
        final CacheManager cacheManager = initCacheManager();

        //create a cache with the provided name
        final Cache<String, Integer> cache = initCache("theCache", cacheManager);

        //lets populate the content
        populateCache(cache);

        //so we print the content whatever we have
        printContent(cache);

        //lets wait for 10 sec to expire the content
        sleepFor(10 * 1000);

        //and print the content again, and see everything has expired and values are null
        printContent(cache);

        //lastly shutdown the cache manager
        shutdown();
    }



}
