package com.hazelcast.cache.wanreplication;

import com.hazelcast.cache.HazelcastCachingProvider;
import com.hazelcast.cache.ICache;
import com.hazelcast.cache.impl.AbstractHazelcastCacheManager;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

public class EnterpriseCacheWanReplicationClusterB {

    private static final String LICENSE_KEY = "YOUR_LICENSE_KEY";

    private static HazelcastInstance clusterB;

    public static void main(String[] args) throws Exception {
        System.setProperty("hazelcast.jcache.provider.type", "server");
        new EnterpriseCacheWanReplicationClusterB().start();
    }

    private void start() throws Exception {
        initClusters();
        waitUntilClusterSafe();
        Scanner reader = new Scanner(System.in);

        CachingProvider provider = Caching.getCachingProvider();
        Properties properties = HazelcastCachingProvider.propertiesByInstanceName(clusterB.getConfig().getInstanceName());
        URI cacheManagerName;
        try {
            cacheManagerName = new URI("my-cache-manager");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        AbstractHazelcastCacheManager manager = (AbstractHazelcastCacheManager) provider.getCacheManager(cacheManagerName,
                clusterB.getConfig().getClassLoader(), properties);
        CacheConfig cacheConfig = new CacheConfig(clusterB.getConfig().getCacheConfig("default"));
        ICache<Object, Object> cache = manager.getOrCreateCache("default", cacheConfig);

        System.out.println("Cluster is ready now.");
        System.out.println("write \"help\" for the command lists:");
        for (; ; ) {
            Thread.sleep(100);
            System.out.println("Command:");
            String command = reader.nextLine();
            if (command.equals("help")) {
                printHelpCommands();
            }
            String key;
            if (command.startsWith("get")) {
                key = command.split(" ")[1];
                System.out.println(cache.get(key));
            }
            if (command.startsWith("put ")) {
                key = command.split(" ")[1];
                String value = command.split(" ")[2];
                cache.put(key, value);
            }
            if (command.startsWith("putmany")) {
                key = command.split(" ")[1];
                int start = new Random().nextInt();
                for (int i = start; i < start + Integer.parseInt(key); i++) {
                    cache.put(i, i);
                }
            }
        }
    }

    private void printHelpCommands() {
        System.out.println("Commands:\n"
                + "1) get [key]\n"
                + "2) put [key] [value]\n"
                + "3) putmany [number]\n");
    }

    private void waitUntilClusterSafe() throws InterruptedException {
        while (!clusterB.getPartitionService().isClusterSafe()) {
            Thread.sleep(100);
        }
    }

    private void initClusters() {
        clusterB = Hazelcast.newHazelcastInstance(getConfigClusterB());
    }

    private Config getConfigClusterB() {
        final Config config = new Config();
        config.setLicenseKey(LICENSE_KEY).getGroupConfig().setName("clusterB").setPassword("clusterB-pass");
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true).addMember("127.0.0.1:5702");
        config.setInstanceName("clusterB");
        config.getNetworkConfig().setPort(5702);
        config.setClassLoader(createCacheManagerClassLoader());

        return config;
    }

    private CacheManagerClassLoader createCacheManagerClassLoader() {
        URLClassLoader currentClassLoader = (URLClassLoader) getClass().getClassLoader();
        return new CacheManagerClassLoader(currentClassLoader.getURLs(), currentClassLoader);
    }

    private class CacheManagerClassLoader extends URLClassLoader {

        CacheManagerClassLoader(URL[] urls, ClassLoader classLoader) {
            super(urls, classLoader);
        }
    }
}
