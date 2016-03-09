import com.hazelcast.cache.ICache;
import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.NativeMemoryConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.memory.MemorySize;
import com.hazelcast.memory.MemoryUnit;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;

/**
 * HiDensity cache usage example
 */
abstract class HiDensityCacheUsageSupport {

    private static final String LICENSE_KEY;

    /**
     * It is advised using native memory (off-heap) serialization
     * because you want no more GC since you are using Hi-Density cache.
     * In native memory (off-heap) serialization, serializations in Hazelcast (operations, etc ...)
     * is done on a natively allocated memory (off-heap) instead of using byte array for storing byte stream.
     *
     * It is not necessary but it is a good practice for Hi-Density cache usage.
     * "USE_NATIVE_MEMORY_SERIALIZATION" is "false" by default since there is no guarantee that
     * "sun.misc.Unsafe" is available. But if you sure that "sun.misc.Unsafe" is available
     * (by the way you can also check it by "com.hazelcast.nio.UnsafeHelper.UNSAFE_AVAILABLE"),
     * it is advised that set "USE_NATIVE_MEMORY_SERIALIZATION" to "true.
     *
     * Note: This field is not defined as final and can be updated in
     * any concrete implementation of "HiDensityCacheUsageSupport".
     */
    @SuppressWarnings("checkstyle:explicitinitialization")
    private static boolean useNativeMemorySerialization = false;

    static {
        // Pass your license key as system property like
        // "-Dhazelcast.enterprise.license.key=<YOUR_LICENCE_KEY_HERE>"
        LICENSE_KEY = System.getProperty("hazelcast.enterprise.license.key");
    }

    private static HazelcastInstance instance;
    private static CachingProvider cachingProvider;
    private static CacheManager cacheManager;

    private static Config createConfig() {
        return new Config()
                .setLicenseKey(LICENSE_KEY)
                .setNativeMemoryConfig(createMemoryConfig())
                .setSerializationConfig(createSerializationConfig());
    }

    private static CacheConfig createCacheConfig(String cacheName) {
        return (CacheConfig) new CacheConfig()
                .setEvictionConfig(createEvictionConfig())
                .setInMemoryFormat(InMemoryFormat.NATIVE)
                .setName(cacheName)
                .setStatisticsEnabled(true);
    }

    private static EvictionConfig createEvictionConfig() {
        return new EvictionConfig()
                // %90 percentage of native memory can be used
                .setSize(90)
                .setMaximumSizePolicy(EvictionConfig.MaxSizePolicy.USED_NATIVE_MEMORY_PERCENTAGE);
    }

    private static NativeMemoryConfig createMemoryConfig() {
        MemorySize memorySize = new MemorySize(512, MemoryUnit.MEGABYTES);
        return new NativeMemoryConfig()
                .setAllocatorType(NativeMemoryConfig.MemoryAllocatorType.POOLED)
                .setSize(memorySize)
                .setEnabled(true)
                .setMinBlockSize(16)
                .setPageSize(1 << 20);
    }

    private static SerializationConfig createSerializationConfig() {
        SerializationConfig serializationConfig = new SerializationConfig();
        if (useNativeMemorySerialization) {
            serializationConfig = serializationConfig
                    // use native memory (off-heap) based storage for holding byte stream
                    .setAllowUnsafe(true)
                    // use native byte order of JVM/OS to prevent extra byte order convertions
                    .setUseNativeByteOrder(true);
        }
        return serializationConfig;
    }

    static ICache createCache(String cacheName) {
        Cache<Object, Object> cache = cacheManager.createCache(cacheName, createCacheConfig(cacheName));
        return cache.unwrap(ICache.class);
    }

    static void init() {
        instance = createInstance(createConfig());
        cachingProvider =
                HazelcastServerCachingProvider
                        .createCachingProvider(instance);
        cacheManager = cachingProvider.getCacheManager();
    }

    static void destroy() {
        if (cacheManager != null) {
            Iterable<String> cacheNames = cacheManager.getCacheNames();
            for (String name : cacheNames) {
                cacheManager.destroyCache(name);
            }
        }
        if (instance != null) {
            instance.shutdown();
        }
    }

    private static HazelcastInstance createInstance(Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }
}
