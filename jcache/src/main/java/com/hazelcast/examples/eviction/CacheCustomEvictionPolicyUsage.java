package com.hazelcast.examples.eviction;

import com.hazelcast.cache.CacheEntryView;
import com.hazelcast.cache.CacheEvictionPolicyComparator;
import com.hazelcast.cache.ICache;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.EvictionConfig;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

/**
 * Demonstrates how to use custom eviction policy in JCache.
 */
public class CacheCustomEvictionPolicyUsage {

    private static final boolean LOG_ENABLED = Boolean.getBoolean("cacheCustomEvictionPolicyUsage.logEnabled");

    static {
        System.setProperty("hazelcast.jcache.provider.type", "server");
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        // Use eviction policy comparator by its instance
        CacheConfig cacheConfig1 = createCacheConfig("MyCache_1", false);
        ICache<Integer, String> cache1 = (ICache<Integer, String>) cacheManager.createCache("MyCache_1", cacheConfig1);

        // Default max-size for eviction is 10K.
        // With partition based cluster wide approximation, after almost 15K elements (for default 271 partition),
        // it is guaranteed that there will be eviction.
        for (int i = 0; i < 20000; i++) {
            cache1.put(i, "Value-" + i);
        }

        // Use eviction policy comparator by its name
        CacheConfig cacheConfig2 = createCacheConfig("MyCache_2", true);
        ICache<Integer, String> cache2 = (ICache<Integer, String>) cacheManager.createCache("MyCache_2", cacheConfig2);

        // Default max-size for eviction is 10K.
        // With partition based cluster wide approximation, after almost 15K elements (for default 271 partition),
        // it is guaranteed that there will be eviction.
        for (int i = 0; i < 20000; i++) {
            cache2.put(i, "Value-" + i);
        }

        cachingProvider.close();
    }

    private static CacheConfig createCacheConfig(String cacheName, boolean evictionPolicyComparatorByName) {
        EvictionConfig evictionConfig = evictionPolicyComparatorByName
                ? new EvictionConfig().setComparatorClassName(MyEvictionPolicyComparator.class.getName())
                : new EvictionConfig().setComparator(new MyEvictionPolicyComparator());
        return new CacheConfig()
                .setName(cacheName)
                .setEvictionConfig(evictionConfig);
    }

    private static class MyEvictionPolicyComparator extends CacheEvictionPolicyComparator<Integer, String> {

        @Override
        public int compare(CacheEntryView<Integer, String> e1, CacheEntryView<Integer, String> e2) {
            Integer key1 = e1.getKey();
            Integer key2 = e2.getKey();
            if (LOG_ENABLED) {
                System.out.println("Comparing entries with key " + key1 + " and with key " + key2
                        + " to select the one with higher priority to be evicted");
            }
            if (key2 > key1) {
                // -1
                return FIRST_ENTRY_HAS_HIGHER_PRIORITY_TO_BE_EVICTED;
            } else if (key2 < key1) {
                // +1
                return SECOND_ENTRY_HAS_HIGHER_PRIORITY_TO_BE_EVICTED;
            } else {
                // 0
                return BOTH_OF_ENTRIES_HAVE_SAME_PRIORITY_TO_BE_EVICTED;
            }
        }
    }
}
