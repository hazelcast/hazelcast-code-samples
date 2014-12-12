import com.hazelcast.cache.ICache;
import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.CacheEvictionConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.NativeMemoryConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.GroupProperties;
import com.hazelcast.instance.HazelcastInstanceFactory;
import com.hazelcast.memory.MemorySize;
import com.hazelcast.memory.MemoryUnit;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;

/**
 * HiDensity cache usage example
 */
public abstract class HiDensityCacheUsageSupport {

    protected static final String LICENSE_KEY;

    /**
     * It is advised using native memory (off-heap) serialization
     * because you want no more GC since you are using Hi-Density cache.
     * In native memory (off-heap) serialization, serializations in Hazelcast (operations, etc ...)
     * is done on a natively allocated memory (off-heap) instead of using byte array for storing byte stream.
     *
     * It is not necessary but it is a good practice for Hi-Density cache usage.
     * "USE_NATIVE_MEMORY_SERIALIZATION" is "false" by default since there is no guarantee that
     * "sun.misc.Unsafe" is available. But if you sure that "sun.misc.Unsafe" is available
     *  (by the way you can also check it by "com.hazelcast.nio.UnsafeHelper.UNSAFE_AVAILABLE"),
     *  it is advised that set "USE_NATIVE_MEMORY_SERIALIZATION" to "true.
     *
     *  Note: This field is not defined as final and can be updated in
     *        any concrete implementation of "HiDensityCacheUsageSupport".
     */
    protected static boolean USE_NATIVE_MEMORY_SERIALIZATION = false;

    static {
        // Pass your license key as system property like
        // "-Dhazelcast.enterprise.license.key=<YOUR_LICENCE_KEY_HERE>"
        LICENSE_KEY = System.getProperty(GroupProperties.PROP_ENTERPRISE_LICENSE_KEY);
    }

    protected static HazelcastInstance instance;
    protected static CachingProvider cachingProvider;
    protected static CacheManager cacheManager;

    protected static Config createConfig() {
        return
                new Config()
                        .setLicenseKey(LICENSE_KEY)
                        .setNativeMemoryConfig(createMemoryConfig())
                        .setSerializationConfig(createSerializationConfig());
    }

    protected static CacheConfig createCacheConfig(String cacheName) {
        return
                (CacheConfig) new CacheConfig()
                        .setEvictionConfig(createEvictionConfig())
                        .setInMemoryFormat(InMemoryFormat.NATIVE)
                        .setName(cacheName)
                        .setStatisticsEnabled(true);
    }

    protected static CacheEvictionConfig createEvictionConfig() {
        return
                new CacheEvictionConfig()
                        .setSize(90) // %90 percentage of native memory can be used
                        .setMaxSizePolicy(CacheEvictionConfig.CacheMaxSizePolicy.USED_NATIVE_MEMORY_PERCENTAGE);
    }

    protected static NativeMemoryConfig createMemoryConfig() {
        MemorySize memorySize = new MemorySize(512, MemoryUnit.MEGABYTES);
        return
                new NativeMemoryConfig()
                        .setAllocatorType(NativeMemoryConfig.MemoryAllocatorType.POOLED)
                        .setSize(memorySize)
                        .setEnabled(true)
                        .setMinBlockSize(16)
                        .setPageSize(1 << 20);
    }

    protected static SerializationConfig createSerializationConfig() {
        SerializationConfig serializationConfig = new SerializationConfig();
        if (USE_NATIVE_MEMORY_SERIALIZATION) {
            serializationConfig =
                    serializationConfig
                            // Use native memory (off-heap) based storage for holding byte stream
                            .setAllowUnsafe(true)
                            // Use native byte order of JVM/OS to prevent extra byte order convertions
                            .setUseNativeByteOrder(true);
        }
        return serializationConfig;
    }

    protected static ICache createCache(String cacheName) {
        Cache<Object, Object> cache =
                cacheManager.createCache(cacheName, createCacheConfig(cacheName));
        return cache.unwrap(ICache.class);
    }

    protected static HazelcastInstance createInstance(Config config) {
        return HazelcastInstanceFactory.newHazelcastInstance(config);
    }

    protected static void init() {
        instance = createInstance(createConfig());
        cachingProvider =
                HazelcastServerCachingProvider
                        .createCachingProvider(instance);
        cacheManager = cachingProvider.getCacheManager();
    }

    protected static void destroy() {
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

}
