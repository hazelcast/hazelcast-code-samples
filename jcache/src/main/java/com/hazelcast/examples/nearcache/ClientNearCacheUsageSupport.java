package com.hazelcast.examples.nearcache;

import com.hazelcast.cache.ICache;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.cache.impl.HazelcastClientCacheManager;
import com.hazelcast.client.cache.impl.HazelcastClientCachingProvider;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.impl.HazelcastClientProxy;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import javax.cache.spi.CachingProvider;
import java.util.LinkedList;
import java.util.List;

public abstract class ClientNearCacheUsageSupport {

    protected static final String DEFAULT_CACHE_NAME = "ClientCache";

    protected final InMemoryFormat inMemoryFormat;

    protected HazelcastInstance serverInstance;
    protected List<HazelcastInstance> clients = new LinkedList<HazelcastInstance>();

    protected ClientNearCacheUsageSupport() {
        inMemoryFormat = InMemoryFormat.BINARY;
        init();
    }

    protected ClientNearCacheUsageSupport(InMemoryFormat defaultInMemoryFormat) {
        inMemoryFormat = defaultInMemoryFormat;
        init();
    }

    protected void init() {
        serverInstance = Hazelcast.newHazelcastInstance(createConfig());
    }

    public void shutdown() {
        for (HazelcastInstance client : clients) {
            client.shutdown();
        }
        clients.clear();
        if (serverInstance != null) {
            serverInstance.shutdown();
        }
    }

    protected Config createConfig() {
        Config config = new Config();
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getAwsConfig().setEnabled(false);
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(false);
        return config;
    }

    protected ClientConfig createClientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("127.0.0.1");
        return clientConfig;
    }

    protected CacheConfig createCacheConfig() {
        return createCacheConfig(DEFAULT_CACHE_NAME, inMemoryFormat);
    }

    protected CacheConfig createCacheConfig(String cacheName) {
        return createCacheConfig(cacheName, inMemoryFormat);
    }

    protected CacheConfig createCacheConfig(InMemoryFormat inMemoryFormat) {
        return createCacheConfig(DEFAULT_CACHE_NAME, inMemoryFormat);
    }

    protected CacheConfig createCacheConfig(String cacheName, InMemoryFormat inMemoryFormat) {
        return new CacheConfig()
                .setName(DEFAULT_CACHE_NAME)
                .setInMemoryFormat(inMemoryFormat);
    }

    protected NearCacheConfig createNearCacheConfig() {
        return createNearCacheConfig(DEFAULT_CACHE_NAME, inMemoryFormat);
    }

    protected NearCacheConfig createNearCacheConfig(String cacheName) {
        return createNearCacheConfig(cacheName, inMemoryFormat);
    }

    protected NearCacheConfig createNearCacheConfig(InMemoryFormat inMemoryFormat) {
        return createNearCacheConfig(DEFAULT_CACHE_NAME, inMemoryFormat);
    }

    protected NearCacheConfig createNearCacheConfig(String cacheName, InMemoryFormat inMemoryFormat) {
        return new NearCacheConfig()
                .setName(DEFAULT_CACHE_NAME)
                .setInMemoryFormat(inMemoryFormat);
    }

    protected <K, V> ICache<K, V> createCacheWithNearCache() {
        return createCacheWithNearCache(DEFAULT_CACHE_NAME, createNearCacheConfig());
    }

    protected <K, V> ICache<K, V> createCacheWithNearCache(String cacheName) {
        return createCacheWithNearCache(cacheName, createNearCacheConfig(cacheName));
    }

    protected <K, V> ICache<K, V> createCacheWithNearCache(InMemoryFormat inMemoryFormat) {
        return createCacheWithNearCache(DEFAULT_CACHE_NAME, createNearCacheConfig(inMemoryFormat));
    }

    protected <K, V> ICache<K, V> createCacheWithNearCache(NearCacheConfig nearCacheConfig) {
        return createCacheWithNearCache(DEFAULT_CACHE_NAME, nearCacheConfig);
    }

    protected <K, V> ICache<K, V> createCacheWithNearCache(String cacheName, NearCacheConfig nearCacheConfig) {
        CacheConfig<K, V> cacheConfig = createCacheConfig(nearCacheConfig.getInMemoryFormat());
        return createCacheWithNearCache(cacheName, cacheConfig, nearCacheConfig);
    }

    protected <K, V> ICache<K, V> createCacheWithNearCache(CacheConfig<K, V> cacheConfig,
                                                           NearCacheConfig nearCacheConfig) {
        return createCacheWithNearCache(DEFAULT_CACHE_NAME, cacheConfig, nearCacheConfig);
    }

    protected <K, V> ICache<K, V> createCacheWithNearCache(String cacheName, CacheConfig<K, V> cacheConfig,
                                                           NearCacheConfig nearCacheConfig) {
        ClientConfig clientConfig = createClientConfig();
        clientConfig.addNearCacheConfig(nearCacheConfig);
        HazelcastClientProxy client = (HazelcastClientProxy) HazelcastClient.newHazelcastClient(clientConfig);
        CachingProvider provider = HazelcastClientCachingProvider.createCachingProvider(client);
        HazelcastClientCacheManager cacheManager = (HazelcastClientCacheManager) provider.getCacheManager();

        ICache<K, V> cache = cacheManager.createCache(cacheName, cacheConfig);

        clients.add(client);

        return cache;
    }

    protected <K, V> ICache<K, V> getCacheWithNearCache() {
        return getCacheWithNearCache(DEFAULT_CACHE_NAME, createNearCacheConfig());
    }

    protected <K, V> ICache<K, V> getCacheWithNearCache(String cacheName) {
        return getCacheWithNearCache(cacheName, createNearCacheConfig(cacheName));
    }

    protected <K, V> ICache<K, V> getCacheWithNearCache(InMemoryFormat inMemoryFormat) {
        return getCacheWithNearCache(DEFAULT_CACHE_NAME, createNearCacheConfig(inMemoryFormat));
    }

    protected <K, V> ICache<K, V> getCacheWithNearCache(NearCacheConfig nearCacheConfig) {
        return getCacheWithNearCache(DEFAULT_CACHE_NAME, nearCacheConfig);
    }

    protected <K, V> ICache<K, V> getCacheWithNearCache(String cacheName, NearCacheConfig nearCacheConfig) {
        ClientConfig clientConfig = createClientConfig();
        clientConfig.addNearCacheConfig(nearCacheConfig);
        HazelcastClientProxy client = (HazelcastClientProxy) HazelcastClient.newHazelcastClient(clientConfig);
        CachingProvider provider = HazelcastClientCachingProvider.createCachingProvider(client);
        HazelcastClientCacheManager cacheManager = (HazelcastClientCacheManager) provider.getCacheManager();

        ICache<K, V> cache = cacheManager.getCache(cacheName);

        clients.add(client);

        return cache;
    }

    protected String generateValueFromKey(Integer key) {
        return "Value-" + key;
    }

    protected void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
