package com.hazelcast.examples.nearcache;

import com.hazelcast.cache.ICache;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.examples.Article;

public class ClientNearCacheWithEviction extends ClientNearCacheUsageSupport {

    private static final int RECORD_COUNT = 100;

    public void run() {
        NearCacheConfig nearCacheConfig = createNearCacheConfig()
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setInvalidateOnChange(false)
                .setEvictionConfig(createEvictionConfigWithEntryCountPolicy(RECORD_COUNT));

        ICache<Integer, Article> cache = createCacheWithNearCache(nearCacheConfig);

        for (int i = 1; i <= RECORD_COUNT; i++) {
            cache.put(i, new Article("foo" + i));
        }
        printNearCacheStats(cache, "The put(1..100, article) calls have no effect on the empty Near Cache");

        for (int i = 1; i <= RECORD_COUNT; i++) {
            cache.get(i);
        }
        printNearCacheStats(cache, "The first get(1..100) calls populate the Near Cache");

        for (int i = 1; i <= RECORD_COUNT; i++) {
            cache.get(i);
        }
        printNearCacheStats(cache, "The second get(1..100) calls are served from the Near Cache");

        cache.put(101, new Article("foo101"));
        printNearCacheStats(cache, "The put(101, article) call has no effect on the populated Near Cache");

        cache.get(101);
        printNearCacheStats(cache, "The first get(101) call triggers the eviction and population of the Near Cache");

        cache.get(101);
        printNearCacheStats(cache, "The second get(101) call is served from the Near Cache");

        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }

    public static void main(String[] args) {
        ClientNearCacheWithEviction clientNearCacheUsage = new ClientNearCacheWithEviction();
        clientNearCacheUsage.run();
    }
}
