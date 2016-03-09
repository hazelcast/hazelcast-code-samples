package nearcache;

import com.hazelcast.cache.ICache;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.cache.impl.HazelcastClientCacheManager;
import com.hazelcast.client.cache.impl.HazelcastClientCachingProvider;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.impl.HazelcastClientProxy;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.NativeMemoryConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.examples.nearcache.ClientNearCacheUsageSupport;
import com.hazelcast.memory.HazelcastMemoryManager;
import com.hazelcast.memory.MemorySize;
import com.hazelcast.memory.MemoryUnit;
import com.hazelcast.nio.serialization.EnterpriseSerializationService;

import javax.cache.spi.CachingProvider;

abstract class ClientHiDensityNearCacheUsageSupport extends ClientNearCacheUsageSupport {

    // Pass your license key as system property like
    // "-Dhazelcast.enterprise.license.key=<YOUR_LICENCE_KEY_HERE>"
    private static final String LICENSE_KEY = System.getProperty("hazelcast.enterprise.license.key");

    private static final MemorySize SERVER_NATIVE_MEMORY_SIZE = new MemorySize(256, MemoryUnit.MEGABYTES);
    private static final MemorySize CLIENT_NATIVE_MEMORY_SIZE = new MemorySize(128, MemoryUnit.MEGABYTES);

    ClientHiDensityNearCacheUsageSupport() {
        super(InMemoryFormat.NATIVE);
    }

    protected Config createConfig() {
        Config config = super.createConfig();
        NativeMemoryConfig nativeMemoryConfig = new NativeMemoryConfig()
                .setSize(SERVER_NATIVE_MEMORY_SIZE)
                .setEnabled(true);
        config.setNativeMemoryConfig(nativeMemoryConfig);
        config.setLicenseKey(LICENSE_KEY);
        return config;
    }

    protected ClientConfig createClientConfig() {
        ClientConfig clientConfig = super.createClientConfig();
        NativeMemoryConfig nativeMemoryConfig = new NativeMemoryConfig()
                .setSize(CLIENT_NATIVE_MEMORY_SIZE)
                .setEnabled(true);
        clientConfig.setNativeMemoryConfig(nativeMemoryConfig);
        clientConfig.setLicenseKey(LICENSE_KEY);
        return clientConfig;
    }

    @Override
    protected CacheConfig createCacheConfig(String cacheName, InMemoryFormat inMemoryFormat) {
        inMemoryFormat = InMemoryFormat.NATIVE;
        CacheConfig cacheConfig = super.createCacheConfig(cacheName, inMemoryFormat);
        if (inMemoryFormat == InMemoryFormat.NATIVE) {
            EvictionConfig evictionConfig = new EvictionConfig();
            evictionConfig.setMaximumSizePolicy(EvictionConfig.MaxSizePolicy.USED_NATIVE_MEMORY_PERCENTAGE);
            evictionConfig.setSize(99);
            cacheConfig.setEvictionConfig(evictionConfig);
        }
        return cacheConfig;
    }

    @Override
    protected NearCacheConfig createNearCacheConfig(String cacheName, InMemoryFormat inMemoryFormat) {
        NearCacheConfig nearCacheConfig = super.createNearCacheConfig(cacheName, inMemoryFormat);
        EvictionConfig evictionConfig = new EvictionConfig();
        if (inMemoryFormat == InMemoryFormat.NATIVE) {
            evictionConfig.setMaximumSizePolicy(EvictionConfig.MaxSizePolicy.USED_NATIVE_MEMORY_PERCENTAGE);
            evictionConfig.setSize(99);
            nearCacheConfig.setEvictionConfig(evictionConfig);
        }
        return nearCacheConfig;
    }

    class HiDensityNearCacheSupportContext<K, V> {

        final ICache<K, V> cache;
        final HazelcastMemoryManager memoryManager;

        HiDensityNearCacheSupportContext(ICache<K, V> cache, HazelcastMemoryManager memoryManager) {
            this.cache = cache;
            this.memoryManager = memoryManager;
        }

    }

    <K, V> HiDensityNearCacheSupportContext<K, V> createHiDensityCacheWithHiDensityNearCache() {
        return createHiDensityCacheWithHiDensityNearCache(DEFAULT_CACHE_NAME, createNearCacheConfig());
    }

    <K, V> HiDensityNearCacheSupportContext<K, V> createHiDensityCacheWithHiDensityNearCache(String cacheName) {
        return createHiDensityCacheWithHiDensityNearCache(cacheName, createNearCacheConfig(cacheName));
    }

    <K, V> HiDensityNearCacheSupportContext<K, V> createHiDensityCacheWithHiDensityNearCache(
            InMemoryFormat inMemoryFormat) {
        return createHiDensityCacheWithHiDensityNearCache(DEFAULT_CACHE_NAME, createNearCacheConfig(inMemoryFormat));
    }

    <K, V> HiDensityNearCacheSupportContext<K, V> createHiDensityCacheWithHiDensityNearCache(String cacheName,
                                                                                             NearCacheConfig nearCacheConfig) {
        ClientConfig clientConfig = createClientConfig();
        clientConfig.addNearCacheConfig(nearCacheConfig);
        HazelcastClientProxy client = (HazelcastClientProxy) HazelcastClient.newHazelcastClient(clientConfig);
        CachingProvider provider = HazelcastClientCachingProvider.createCachingProvider(client);
        HazelcastClientCacheManager cacheManager = (HazelcastClientCacheManager) provider.getCacheManager();

        CacheConfig<K, V> cacheConfig = createCacheConfig(nearCacheConfig.getInMemoryFormat());
        ICache<K, V> cache = cacheManager.createCache(cacheName, cacheConfig);

        clients.add(client);

        EnterpriseSerializationService enterpriseSerializationService =
                (EnterpriseSerializationService) client.getSerializationService();

        return new HiDensityNearCacheSupportContext(cache, enterpriseSerializationService.getMemoryManager());
    }
}
