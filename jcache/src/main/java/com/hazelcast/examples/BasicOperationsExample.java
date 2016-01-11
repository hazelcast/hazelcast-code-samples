package com.hazelcast.examples;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * Basic example
 * Configures a cache with access expiry of 10 secs.
 */
public class BasicOperationsExample extends AbstractApp {

    public static void main(String[] args) throws InterruptedException {
        new BasicOperationsExample().runApp();
    }

    private void runApp() throws InterruptedException {
        // thin is we need to initialize the cache Manager
        final CacheManager cacheManager = initCacheManager();

        // create a cache with the provided name
        final Cache<String, Integer> cache = initCache("theCache", cacheManager);

        // populate the content
        populateCache(cache);

        // print the content whatever we have
        printContent(cache);

        // wait for 10 sec to expire the content
        sleepFor(10 * 1000);

        // print the content again, and see everything has expired and values are null
        printContent(cache);

        // shutdown the cache manager
        shutdown();
    }
}
