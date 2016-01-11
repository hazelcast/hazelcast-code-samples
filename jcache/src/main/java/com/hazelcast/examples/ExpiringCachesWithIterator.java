package com.hazelcast.examples;

import javax.cache.Cache;
import javax.cache.CacheManager;

public class ExpiringCachesWithIterator extends AbstractApp {

    public static void main(String[] args) throws InterruptedException {
        new ExpiringCachesWithIterator().runApp();
    }

    private void runApp() throws InterruptedException {
        // initialize the CacheManager
        CacheManager cacheManager = initCacheManager();

        // create a cache with the provided name
        Cache<String, Integer> cache = initCache("theCache", cacheManager);

        // populate the content
        populateCache(cache);

        // print the content whatever we have
        printContent(cache);

        // wait for 10 sec to expire the content
        sleepFor(10 * 1000);

        // print the content again and see everything has expired and we have no values
        printContentWithIterator(cache);

        // shutdown the CacheManager
        shutdown();
    }
}
