import com.hazelcast.cache.ICache;
import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.CacheEvictionConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.NativeMemoryConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.core.HazelcastInstance;
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

    protected static final String LICENSE_KEY = "---- YOUR LICENCE KEY HERE ----";

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
        return
                new SerializationConfig()
                        .setAllowUnsafe(true)
                        .setUseNativeByteOrder(true);
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
