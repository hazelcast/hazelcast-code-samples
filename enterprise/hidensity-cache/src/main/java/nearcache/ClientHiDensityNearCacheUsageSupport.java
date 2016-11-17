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
import com.hazelcast.config.EvictionConfig.MaxSizePolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.NativeMemoryConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.examples.nearcache.ClientNearCacheUsageSupport;
import com.hazelcast.memory.HazelcastMemoryManager;
import com.hazelcast.memory.MemorySize;
import com.hazelcast.memory.MemoryUnit;
import com.hazelcast.nio.serialization.EnterpriseSerializationService;

import javax.cache.spi.CachingProvider;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

@SuppressWarnings("unused")
abstract class ClientHiDensityNearCacheUsageSupport extends ClientNearCacheUsageSupport {

    private static final MemorySize SERVER_NATIVE_MEMORY_SIZE = new MemorySize(256, MemoryUnit.MEGABYTES);
    private static final MemorySize CLIENT_NATIVE_MEMORY_SIZE = new MemorySize(128, MemoryUnit.MEGABYTES);

    ClientHiDensityNearCacheUsageSupport() {
        super(InMemoryFormat.NATIVE);
    }

    @Override
    protected Config createConfig() {
        NativeMemoryConfig nativeMemoryConfig = new NativeMemoryConfig()
                .setSize(SERVER_NATIVE_MEMORY_SIZE)
                .setEnabled(true);

        return super.createConfig()
                .setLicenseKey(ENTERPRISE_LICENSE_KEY)
                .setNativeMemoryConfig(nativeMemoryConfig);
    }

    @Override
    protected ClientConfig createClientConfig() {
        NativeMemoryConfig nativeMemoryConfig = new NativeMemoryConfig()
                .setSize(CLIENT_NATIVE_MEMORY_SIZE)
                .setEnabled(true);

        return super.createClientConfig()
                .setLicenseKey(ENTERPRISE_LICENSE_KEY)
                .setNativeMemoryConfig(nativeMemoryConfig);
    }

    @Override
    protected <K, V> CacheConfig<K, V> createCacheConfig(String cacheName, InMemoryFormat inMemoryFormat) {
        EvictionConfig evictionConfig = new EvictionConfig()
                .setMaximumSizePolicy(MaxSizePolicy.USED_NATIVE_MEMORY_PERCENTAGE)
                .setSize(99);

        CacheConfig<K, V> cacheConfig = super.createCacheConfig(cacheName, inMemoryFormat);
        cacheConfig.setEvictionConfig(evictionConfig);
        return cacheConfig;
    }

    @Override
    protected NearCacheConfig createNearCacheConfig(String cacheName, InMemoryFormat inMemoryFormat) {
        NearCacheConfig nearCacheConfig = super.createNearCacheConfig(cacheName, inMemoryFormat);
        if (inMemoryFormat == InMemoryFormat.NATIVE) {
            EvictionConfig evictionConfig = new EvictionConfig()
                    .setMaximumSizePolicy(MaxSizePolicy.USED_NATIVE_MEMORY_PERCENTAGE)
                    .setSize(99);
            nearCacheConfig.setEvictionConfig(evictionConfig);
        }
        return nearCacheConfig;
    }

    <K, V> HiDensityNearCacheSupportContext<K, V> createHiDensityCacheWithHiDensityNearCache() {
        return createHiDensityCacheWithHiDensityNearCache(DEFAULT_CACHE_NAME, createNearCacheConfig());
    }

    <K, V> HiDensityNearCacheSupportContext<K, V> createHiDensityCacheWithHiDensityNearCache(String cacheName) {
        return createHiDensityCacheWithHiDensityNearCache(cacheName, createNearCacheConfig(cacheName));
    }

    <K, V> HiDensityNearCacheSupportContext<K, V> createHiDensityCacheWithHiDensityNearCache(InMemoryFormat inMemoryFormat) {
        return createHiDensityCacheWithHiDensityNearCache(DEFAULT_CACHE_NAME, createNearCacheConfig(inMemoryFormat));
    }

    <K, V> HiDensityNearCacheSupportContext<K, V> createHiDensityCacheWithHiDensityNearCache(NearCacheConfig nearCacheConfig) {
        return createHiDensityCacheWithHiDensityNearCache(DEFAULT_CACHE_NAME, nearCacheConfig);
    }

    <K, V> HiDensityNearCacheSupportContext<K, V> createHiDensityCacheWithHiDensityNearCache(String cacheName,
                                                                                             NearCacheConfig nearCacheConfig) {
        ClientConfig clientConfig = createClientConfig()
                .addNearCacheConfig(nearCacheConfig);

        HazelcastClientProxy client = (HazelcastClientProxy) HazelcastClient.newHazelcastClient(clientConfig);
        CachingProvider provider = HazelcastClientCachingProvider.createCachingProvider(client);
        HazelcastClientCacheManager cacheManager = (HazelcastClientCacheManager) provider.getCacheManager();

        CacheConfig<K, V> cacheConfig = createCacheConfig(nearCacheConfig.getInMemoryFormat());
        ICache<K, V> cache = cacheManager.createCache(cacheName, cacheConfig);

        clients.add(client);

        EnterpriseSerializationService enterpriseSerializationService =
                (EnterpriseSerializationService) client.getSerializationService();

        return new HiDensityNearCacheSupportContext<K, V>(cache, enterpriseSerializationService.getMemoryManager());
    }

    class HiDensityNearCacheSupportContext<K, V> {

        final ICache<K, V> cache;
        final HazelcastMemoryManager memoryManager;

        HiDensityNearCacheSupportContext(ICache<K, V> cache, HazelcastMemoryManager memoryManager) {
            this.cache = cache;
            this.memoryManager = memoryManager;
        }
    }
}
