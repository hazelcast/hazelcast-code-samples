package com.hazelcast.examples.jcache.client;

import com.hazelcast.examples.AbstractApp;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.expiry.Duration;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * This app will randomly produce and populate the clusters
 */
public class ClientDataProducer extends AbstractApp {

    private final Random random = new Random();

    public static void main(String[] args) throws InterruptedException {
        //ClusterGroup server = new ClusterGroup();
        //server.init();

        new ClientDataProducer().runApp();

        //server.shutdown();
    }

    private void runApp() throws InterruptedException {
        // force client be used as a provider
        clientSetup();

        // first thin is we need to initialize the cache Managers for each cluster
        CacheManager cacheManager1 = initCacheManager(uri1);
        CacheManager cacheManager2 = initCacheManager(uri2);

        //create a cache with the provided name
        Cache<String, Integer> cacheAtCluster1 = initCache("theCache", cacheManager1, new Duration(TimeUnit.SECONDS, 10));
        Cache<String, Integer> cacheAtCluster2 = initCache("theCache", cacheManager2, new Duration(TimeUnit.SECONDS, 2));

        startProducerTask(cacheAtCluster1);
        startProducerTask(cacheAtCluster2);

        System.out.println("Cache data production started...");
    }

    private void startProducerTask(final Cache<String, Integer> cacheAtCluster) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    cacheAtCluster.put(UUID.randomUUID().toString(), random.nextInt());
                    try {
                        Thread.sleep(100);
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
