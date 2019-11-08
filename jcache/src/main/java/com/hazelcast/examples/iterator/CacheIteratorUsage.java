package com.hazelcast.examples.iterator;

import com.hazelcast.cache.ICache;
import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.util.Iterator;

import static com.hazelcast.cache.HazelcastCachingProvider.propertiesByInstanceItself;

/**
 * Code sample to demonstrate iterator usage on cache.
 */
public class CacheIteratorUsage {

    private static boolean isClient = Boolean.getBoolean("com.hazelcast.examples.jcache.iterator.isClient");

    public static void main(String[] args) {
        CachingProvider cachingProvider;
        HazelcastInstance hazelcastInstance;

        if (isClient) {
            Hazelcast.newHazelcastInstance(createConfig());
            hazelcastInstance = HazelcastClient.newHazelcastClient(createClientConfig());
            cachingProvider = Caching.getCachingProvider();
        } else {
            hazelcastInstance = Hazelcast.newHazelcastInstance(createConfig());
            cachingProvider = Caching.getCachingProvider(HazelcastServerCachingProvider.class.getName());
        }

        try {
            CacheManager cacheManager = cachingProvider.getCacheManager(null, null,
                    propertiesByInstanceItself(hazelcastInstance));
            CacheConfig<Integer, String> cacheConfig = new CacheConfig<>("myCache");
            cacheConfig.setTypes(Integer.class, String.class);
            ICache<Integer, String> cache = (ICache<Integer, String>) cacheManager
                    .createCache("myCache", cacheConfig)
                    .unwrap(ICache.class);


            for (int i = 0; i < 200; i++) {
                cache.put(i, "Value of " + i);
            }

            System.out.println("Iterating over default fetch sized iterator...");
            Iterator<Cache.Entry<Integer, String>> iter1 = cache.iterator();
            while (iter1.hasNext()) {
                Cache.Entry<Integer, String> entry = iter1.next();
                System.out.println("\t- Key: " + entry.getKey() + ", Value: " + entry.getValue());
            }

            System.out.println("==============================================-");

            System.out.println("Iterating over specified (25) fetch sized iterator...");
            Iterator<Cache.Entry<Integer, String>> iter2 = cache.iterator(25);
            while (iter2.hasNext()) {
                Cache.Entry<Integer, String> entry = iter2.next();
                System.out.println("\t- Key: " + entry.getKey() + ", Value: " + entry.getValue());
            }
        } finally {
            if (isClient) {
                HazelcastClient.shutdownAll();
            }
            Hazelcast.shutdownAll();
        }
    }

    private static Config createConfig() {
        Config config = new Config();
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getAwsConfig().setEnabled(false);
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(false);
        return config;
    }

    private static ClientConfig createClientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("127.0.0.1");
        return clientConfig;
    }

}
