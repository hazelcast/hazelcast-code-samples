package com.hazelcast.examples.jcache.client;

import com.hazelcast.cache.ICache;
import com.hazelcast.examples.AbstractApp;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.util.Iterator;

/**
 * This will try to iterate over cache values and remove them
 */
public class ClientDataConsumer extends AbstractApp {

    public static void main(String[] args) throws InterruptedException {
        //ClusterGroup server = new ClusterGroup();
        //server.init();

        new ClientDataConsumer().runApp();
        //server.shutdown();
    }

    private void runApp() throws InterruptedException {
        // force client be used as a provider
        clientSetup();

        // first thin is we need to initialize the cache Managers for each cluster
        CacheManager cacheManager1 = initCacheManager(uri1);
        CacheManager cacheManager2 = initCacheManager(uri2);

        // create a cache with the provided name
        Cache<String, Integer> cacheAtCluster1 = cacheManager1.getCache("theCache", String.class, Integer.class);
        Cache<String, Integer> cacheAtCluster2 = cacheManager2.getCache("theCache", String.class, Integer.class);

        startConsumerTask(cacheAtCluster1);
        startConsumerTask(cacheAtCluster2);

        displayCacheSize("cache@c1", (ICache<String, Integer>) cacheAtCluster1);
        displayCacheSize("cache@c2", (ICache<String, Integer>) cacheAtCluster2);
    }

    private void startConsumerTask(final Cache<String, Integer> cacheAtCluster) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    Iterator<Cache.Entry<String, Integer>> iterator = cacheAtCluster.iterator();
                    while (iterator.hasNext()) {
                        Cache.Entry<String, Integer> entry = iterator.next();
                        // value maybe null which means it is expired
                        if (entry.getValue() != null) {
                            // poor entry just remove
                            iterator.remove();
                            // do something with entry, save to db etc.
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        });
        thread.start();
    }

    private void displayCacheSize(final String alias, final ICache<String, Integer> icache) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    System.out.println("Cache: " + alias + " size:" + icache.size());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        });
        thread.start();
    }
}
