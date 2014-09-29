package com.hazelcast.examples;

import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * Created by asim on 29.09.2014.
 */
public class ExpiringCachesWithIterator extends AbstractApp{


    public static void main(String[] args) throws InterruptedException {
        new ExpiringCachesWithIterator().runApp();
    }

    public void runApp()
            throws InterruptedException {
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

        //and print the content again, and see everything has expired and we have no values
        printContentWithIterator(cache);

        //lastly shutdown the cache manager
        shutdown();
    }
}
