package com.hazelcast.examples;

import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

public class ListenerExample extends AbstractApp {


    public ListenerExample() {
    }


    public void registerListener(){
        MyCacheEntryListener<String, Integer> clientListener =  new MyCacheEntryListener<String, Integer>();
        CacheEntryListenerConfiguration<String, Integer> conf = new MutableCacheEntryListenerConfiguration<String, Integer>(FactoryBuilder.factoryOf(clientListener), null, true, false);
        cache.registerCacheEntryListener(conf);
    }

    public void triggerEvents(){
        cache.put("theKey", 66);
        cache.put("theKey", 111);

        cache.remove("theKey");
    }


    public static void main(String[] args) throws InterruptedException {
        ListenerExample app = new ListenerExample();

        app.initCacheManager();
        app.initCache("theCache");

        app.registerListener();

        app.triggerEvents();

        Thread.sleep(5000);
        app.shutdown();


    }
}
