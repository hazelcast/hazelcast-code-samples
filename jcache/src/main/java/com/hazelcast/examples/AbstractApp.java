package com.hazelcast.examples;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Base application
 */
public class AbstractApp {

    static {
        String logging = "hazelcast.logging.type";
        if (System.getProperty(logging) == null) {
            System.setProperty(logging, "jdk");
        }

        System.setProperty("hazelcast.version.check.enabled", "false");
        System.setProperty("hazelcast.mancenter.enabled", "false");
        System.setProperty("hazelcast.wait.seconds.before.join", "1");
        System.setProperty("hazelcast.local.localAddress", "127.0.0.1");
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("hazelcast.jmx", "true");

        // randomize multicast group...
        Random rand = new Random();
        int g1 = rand.nextInt(255);
        int g2 = rand.nextInt(255);
        int g3 = rand.nextInt(255);
        System.setProperty("hazelcast.multicast.group", "224." + g1 + "." + g2 + "." + g3);
        System.setProperty("hazelcast.jcache.provider.type", "server");
    }

    private static final Duration TEN_SEC = new Duration(TimeUnit.SECONDS, 10);

    protected final URI uri1 = new File("jcache/src/main/resources/hazelcast-client-c1.xml").toURI();
    protected final URI uri2 = new File("jcache/src/main/resources/hazelcast-client-c2.xml").toURI();

    private CachingProvider cachingProvider;

    /**
     * initialize the JCache Manager that we will use for creating and getting a cache object
     */
    protected CacheManager initCacheManager(URI uri) {
        //resolve a cache manager
        cachingProvider = Caching.getCachingProvider();
        return cachingProvider.getCacheManager(uri, null);
    }

    protected CacheManager initCacheManager() {
        return initCacheManager(null);
    }

    /**
     * we initialize a cache with name
     */
    protected Cache<String, Integer> initCache(String name, CacheManager cacheManager) {
        // configure the cache
        MutableConfiguration<String, Integer> config = new MutableConfiguration<String, Integer>();
        config.setStoreByValue(true)
                .setTypes(String.class, Integer.class)
                .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(TEN_SEC))
                .setStatisticsEnabled(false);

        // create the cache
        return cacheManager.createCache(name, config);
    }

    /**
     * we initialize a cache with name
     */
    protected Cache<String, Integer> initCache(String name, CacheManager cacheManager, Duration duration) {
        // configure the cache
        MutableConfiguration<String, Integer> config = new MutableConfiguration<String, Integer>();
        config.setStoreByValue(true)
                .setTypes(String.class, Integer.class)
                .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(duration))
                .setStatisticsEnabled(false);
        if (cacheManager.getCache(name, String.class, Integer.class) != null) {
            // cache should not exist so I will destroy it
            cacheManager.destroyCache(name);
        }
        // create the cache
        return cacheManager.createCache(name, config);
    }

    /**
     * we populate cache with (theKey-i, i)
     */
    protected void populateCache(Cache<String, Integer> cache) {
        if (cache != null) {
            for (int i = 0; i < 10; i++) {
                cache.put("theKey-" + i, i);
            }
        }
    }

    /**
     * print all of the content of the cache, if expires or not exist you will see a null value
     */
    protected void printContent(Cache<String, Integer> cache) {
        System.out.println("==============>  " + cache.getName() + "@ URI:" + cache.getCacheManager().getURI()
                + "  <=====================");
        for (int i = 0; i < 10; i++) {
            final String key = "theKey-" + i;
            System.out.println("Key: " + key + ", Value: " + cache.get(key));
        }
        System.out.println("============================================================");
    }

    /**
     * print all of the content of the cache using Iterator, if expires or not exist you will see no value
     */
    void printContentWithIterator(Cache<String, Integer> cache) {
        System.out.println("==============>  " + cache.getName() + "  <=====================");
        Iterator<Cache.Entry<String, Integer>> iterator = cache.iterator();
        while (iterator.hasNext()) {
            Cache.Entry<String, Integer> next = iterator.next();
            System.out.println("Key: " + next.getKey() + ", Value: " + next.getValue());
        }
        System.out.println("============================================================");
    }

    protected void sleepFor(long duration) throws InterruptedException {
        Thread.sleep(duration);
    }

    protected void clientSetup() {
        System.setProperty("hazelcast.jcache.provider.type", "client");
    }

    /**
     * closing the cache manager we started
     */
    public void shutdown() {
        cachingProvider.close();
    }
}
