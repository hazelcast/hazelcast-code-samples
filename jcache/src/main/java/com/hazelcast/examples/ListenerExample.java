package com.hazelcast.examples;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

/**
 * Listener example.
 *
 * Simple example how to register an async listener and
 * execute cache operations to see on console thath events are firing
 */
public class ListenerExample extends AbstractApp {


    public void runApp()
            throws InterruptedException {

        //first thin is we need to initialize the cache Manager
        final CacheManager cacheManager = initCacheManager();

        //create a cache with the provided name
        final Cache<String, Integer> cache = initCache("theCache", cacheManager);

        //registering the CacheEntryListener
        registerListener(cache);

        //doing some operations on the cache to cause event fire
        triggerEvents(cache);

        //lets wait for 5 sec to make sure every event has fired
        sleepFor(5000);

        //lastly shutdown the cache manager
        shutdown();
    }


    public void registerListener(Cache<String, Integer> cache){
        //create the EntryListener
        MyCacheEntryListener<String, Integer> clientListener =  new MyCacheEntryListener<String, Integer>();

        //using out listener, lets create a configuration
        CacheEntryListenerConfiguration<String, Integer> conf = new MutableCacheEntryListenerConfiguration<String, Integer>(FactoryBuilder.factoryOf(clientListener), null, true, false);

        //register it to the cache at run-time
        cache.registerCacheEntryListener(conf);
    }

    public void triggerEvents(Cache<String, Integer> cache) throws InterruptedException {
        //this will fire create event
        cache.put("theKey", 66);

        //but this one will fire an update event as we have it already
        cache.put("theKey", 111);

        //fire remove
        cache.remove("theKey");

        //lets put a value and then access it to start an expiry
        cache.put("theKey", 66);
        cache.get("theKey");

        //lets wait for 10 sec to expire the content
        Thread.sleep(10 * 1000);

        //will force to expire if we access it and fire expire event
        cache.get("theKey");

    }

    public static void main(String[] args) throws InterruptedException {
        new ListenerExample().runApp();
    }



}
