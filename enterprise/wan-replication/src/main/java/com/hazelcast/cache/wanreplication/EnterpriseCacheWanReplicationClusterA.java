package com.hazelcast.cache.wanreplication;

import com.hazelcast.cache.HazelcastCachingProvider;
import com.hazelcast.cache.ICache;
import com.hazelcast.cache.impl.AbstractHazelcastCacheManager;
import com.hazelcast.cache.merge.HigherHitsCacheMergePolicy;
import com.hazelcast.cache.wanreplication.filter.SampleCacheWanEventFilter;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.WanAcknowledgeType;
import com.hazelcast.config.WanPublisherConfig;
import com.hazelcast.config.WanReplicationConfig;
import com.hazelcast.config.WanReplicationRef;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.enterprise.wan.replication.WanBatchReplication;
import com.hazelcast.enterprise.wan.replication.WanReplicationProperties;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

public class EnterpriseCacheWanReplicationClusterA {

    private static final String LICENSE_KEY = "YOUR_LICENSE_KEY";

    private static HazelcastInstance clusterA;

    public static void main(String[] args) throws Exception {
        System.setProperty("hazelcast.jcache.provider.type", "server");

        new EnterpriseCacheWanReplicationClusterA().start();
    }

    private void start() throws Exception {
        initClusters();
        waitUntilClusterSafe();
        Scanner reader = new Scanner(System.in);

        CachingProvider provider = Caching.getCachingProvider();
        Properties properties = HazelcastCachingProvider.propertiesByInstanceName(clusterA.getConfig().getInstanceName());
        URI cacheManagerName;
        try {
            cacheManagerName = new URI("my-cache-manager");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        AbstractHazelcastCacheManager manager = (AbstractHazelcastCacheManager) provider.getCacheManager(cacheManagerName,
                clusterA.getConfig().getClassLoader(), properties);
        CacheConfig config = new CacheConfig(clusterA.getConfig().getCacheConfig("default"));
        ICache<Object, Object> cache = manager.getOrCreateCache("default", config);

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
                    String kv = Integer.toString(i);
                    cache.put(kv, kv);
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
        while (!clusterA.getPartitionService().isClusterSafe()) {
            Thread.sleep(100);
        }
    }

    private void initClusters() {
        clusterA = Hazelcast.newHazelcastInstance(getConfigClusterA());
    }

    private Config getConfigClusterA() {
        Config config = new Config();
        config.setLicenseKey(LICENSE_KEY).getGroupConfig().setName("clusterA").setPassword("clusterA-pass");
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true).addMember("127.0.0.1:5701");
        config.setInstanceName("clusterA");
        config.getNetworkConfig().setPort(5701);
        config.setClassLoader(createCacheManagerClassLoader());
        WanReplicationConfig wanReplicationConfig = new WanReplicationConfig();
        wanReplicationConfig.setName("AtoB");

        WanPublisherConfig publisherConfigClusterB = new WanPublisherConfig();
        publisherConfigClusterB.setClassName(WanBatchReplication.class.getName());
        publisherConfigClusterB.setGroupName("clusterB");
        Map<String, Comparable> props = publisherConfigClusterB.getProperties();
        props.put(WanReplicationProperties.ENDPOINTS.key(), "127.0.0.1:5702");
        props.put(WanReplicationProperties.GROUP_PASSWORD.key(), "clusterB-pass");

        // setting acknowledge type is optional, defaults to ACK_ON_OPERATION_COMPLETE
        props.put(WanReplicationProperties.ACK_TYPE.key(), WanAcknowledgeType.ACK_ON_OPERATION_COMPLETE.name());
        wanReplicationConfig.addWanPublisherConfig(publisherConfigClusterB);

        config.addWanReplicationConfig(wanReplicationConfig);

        WanReplicationRef wanReplicationRef = new WanReplicationRef();
        wanReplicationRef.setName("AtoB");
        config.setLicenseKey(LICENSE_KEY);
        wanReplicationRef.setMergePolicy(HigherHitsCacheMergePolicy.class.getName());
        wanReplicationRef.addFilter(SampleCacheWanEventFilter.class.getName());
        config.getCacheConfig("default").setWanReplicationRef(wanReplicationRef);

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
