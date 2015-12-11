package com.hazelcast.examples.jcache.client;

import com.hazelcast.cache.HazelcastCachingProvider;
import com.hazelcast.cache.ICache;
import com.hazelcast.examples.AbstractApp;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.net.URI;
import java.util.Iterator;
import java.util.Properties;

/**
 * This will try to iterate over cache values and remove them
 */
public class ClientDataConsumer extends AbstractApp{

    public static void main(String[] args) throws InterruptedException {
        //        ClusterGroup server = new ClusterGroup();
        //        server.init();

        new ClientDataConsumer().runApp();

        //        server.shutdown();
    }
    public void runApp()
            throws InterruptedException {

        //Force client be used as a provider
        clientSetup();

        Properties properties1 = HazelcastCachingProvider.propertiesByLocation("classpath:hazelcast-client-c1.xml");
        Properties properties2 = HazelcastCachingProvider.propertiesByLocation("classpath:hazelcast-client-c2.xml");

        //first thin is we need to initialize the cache Managers for each cluster
        final CacheManager cacheManager1 = initCacheManager(uri1, properties1);
        final CacheManager cacheManager2 = initCacheManager(uri2, properties2);

        //create a cache with the provided name
        final Cache<String, Integer> cacheAtCluster1 = cacheManager1.getCache("theCache",String.class, Integer.class);
        final Cache<String, Integer> cacheAtCluster2 = cacheManager2.getCache("theCache",String.class, Integer.class);

        startConsumerTask(cacheAtCluster1);
        startConsumerTask(cacheAtCluster2);

        displayCacheSize("cache@c1", (ICache<String, Integer>) cacheAtCluster1);
        displayCacheSize("cache@c2", (ICache<String, Integer>) cacheAtCluster2);

    }

    public void startConsumerTask(final Cache<String, Integer> cacheAtCluster) {
        if (cacheAtCluster != null) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.interrupted()){
                        final Iterator<Cache.Entry<String, Integer>> iterator = cacheAtCluster.iterator();
                        while (iterator.hasNext()) {
                            final Cache.Entry<String, Integer> entry = iterator.next();
                            //value maybe null which means it is expired
                            if(entry.getValue() != null) {
                                //poor entry just remove
                                iterator.remove();
                                //do something with entry, save to db etc.
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
            t.start();
        } else {
            System.out.println("Provided cache doesn't exist on cluster");
        }
    }

    private void displayCacheSize(final String alias, final ICache<String, Integer> icache) {
        if (icache != null) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.interrupted()){
                        System.out.println("Cache: "+alias+" size:"+icache.size());
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
            });
            t.start();
        }
    }

}
