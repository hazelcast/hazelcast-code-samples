package com.hazelcast.examples.jcache.client;

import com.hazelcast.examples.AbstractApp;

import javax.cache.Cache;
import javax.cache.CacheManager;

import static com.hazelcast.examples.helper.CommonUtils.sleepSeconds;

/**
 * Simple Client cache example
 */
public class SingleClientBasicExample extends AbstractApp {

    public static void main(String[] args) {
        new SingleClientBasicExample().runApp();
    }

    private void runApp() {
        // force client be used as a provider
        clientSetup();

        // first thin is we need to initialize the cache Manager
        final CacheManager cacheManager = initCacheManager();

        // create a cache with the provided name
        final Cache<String, Integer> cache = initCache("theCache", cacheManager);

        // populate the content
        populateCache(cache);

        // print the content whatever we have
        printContent(cache);

        // wait for 10 sec to expire the content
        sleepSeconds(10);

        // print the content again, and see everything has expired and values are null
        printContent(cache);

        // shutdown the cache manager
        shutdown();
    }
}
