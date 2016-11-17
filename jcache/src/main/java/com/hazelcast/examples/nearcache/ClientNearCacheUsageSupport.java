package com.hazelcast.examples.nearcache;

import com.hazelcast.cache.ICache;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.cache.impl.HazelcastClientCacheManager;
import com.hazelcast.client.cache.impl.HazelcastClientCachingProvider;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.impl.HazelcastClientProxy;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.examples.Article;
import com.hazelcast.internal.nearcache.NearCache;
import com.hazelcast.monitor.NearCacheStats;

import javax.cache.spi.CachingProvider;
import java.util.LinkedList;
import java.util.List;

import static com.hazelcast.examples.helper.CommonUtils.sleepSeconds;
import static com.hazelcast.spi.properties.GroupProperty.CACHE_INVALIDATION_MESSAGE_BATCH_FREQUENCY_SECONDS;
import static java.lang.Integer.parseInt;

@SuppressWarnings({"checkstyle:methodcount", "unused"})
public abstract class ClientNearCacheUsageSupport {

    protected static final String DEFAULT_CACHE_NAME = "ClientCache";

    private static final int EXPIRATION_TASK_DELAY_SECONDS = 2 * NearCache.DEFAULT_EXPIRATION_TASK_DELAY_IN_SECONDS;
    private static final int INVALIDATION_DELAY_SECONDS
            = 2 * parseInt(CACHE_INVALIDATION_MESSAGE_BATCH_FREQUENCY_SECONDS.getDefaultValue());

    protected final List<HazelcastInstance> clients = new LinkedList<HazelcastInstance>();

    private final InMemoryFormat inMemoryFormat;
    private final HazelcastInstance serverInstance;

    protected ClientNearCacheUsageSupport() {
        inMemoryFormat = InMemoryFormat.BINARY;
        serverInstance = Hazelcast.newHazelcastInstance(createConfig());
    }

    protected ClientNearCacheUsageSupport(InMemoryFormat defaultInMemoryFormat) {
        inMemoryFormat = defaultInMemoryFormat;
        serverInstance = Hazelcast.newHazelcastInstance(createConfig());
    }

    protected void shutdown() {
        for (HazelcastInstance client : clients) {
            client.shutdown();
        }
        clients.clear();
        if (serverInstance != null) {
            serverInstance.shutdown();
        }
    }

    protected HazelcastInstance getServerInstance() {
        return serverInstance;
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

    protected <K, V> CacheConfig<K, V> createCacheConfig() {
        return createCacheConfig(DEFAULT_CACHE_NAME, inMemoryFormat);
    }

    protected <K, V> CacheConfig<K, V> createCacheConfig(String cacheName) {
        return createCacheConfig(cacheName, inMemoryFormat);
    }

    protected <K, V> CacheConfig<K, V> createCacheConfig(InMemoryFormat inMemoryFormat) {
        return createCacheConfig(DEFAULT_CACHE_NAME, inMemoryFormat);
    }

    protected <K, V> CacheConfig<K, V> createCacheConfig(String cacheName, InMemoryFormat inMemoryFormat) {
        return new CacheConfig<K, V>()
                .setName(DEFAULT_CACHE_NAME)
                .setInMemoryFormat(inMemoryFormat);
    }

    protected EvictionConfig createEvictionConfigWithEntryCountPolicy(int size) {
        return new EvictionConfig()
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setMaximumSizePolicy(EvictionConfig.MaxSizePolicy.ENTRY_COUNT)
                .setSize(size);
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

    protected <K, V> ICache<K, V> createCacheWithNearCache(CacheConfig<K, V> cacheConfig, NearCacheConfig nearCacheConfig) {
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

    protected static void printNearCacheStats(ICache<Integer, Article> cache) {
        NearCacheStats stats = cache.getLocalCacheStatistics().getNearCacheStatistics();

        System.out.printf("The Near Cache contains %d entries.%n", stats.getOwnedEntryCount());
        System.out.printf("The first article instance was retrieved from the remote instance (Near Cache misses: %d).%n",
                stats.getMisses());
        System.out.printf(
                "The second and third article instance were retrieved from the local Near Cache (Near Cache hits: %d).%n",
                stats.getHits());
    }

    protected static void printNearCacheStats(ICache<?, ?> cache, String message) {
        NearCacheStats stats = cache.getLocalCacheStatistics().getNearCacheStatistics();
        System.out.printf("%s (%d entries, %d hits, %d misses, %d evictions, %d expirations)%n",
                message, stats.getOwnedEntryCount(), stats.getHits(), stats.getMisses(),
                stats.getEvictions(), stats.getExpirations());
    }

    protected static void waitForExpirationTask(int expireTimeoutSeconds) {
        sleepSeconds(EXPIRATION_TASK_DELAY_SECONDS + expireTimeoutSeconds);
    }

    protected static void waitForInvalidationEvents() {
        sleepSeconds(INVALIDATION_DELAY_SECONDS);
    }
}
