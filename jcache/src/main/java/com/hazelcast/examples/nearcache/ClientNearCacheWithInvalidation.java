package com.hazelcast.examples.nearcache;

import com.hazelcast.cache.ICache;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.examples.Article;

import static com.hazelcast.examples.helper.HazelcastUtils.generateKeyOwnedBy;

public class ClientNearCacheWithInvalidation extends ClientNearCacheUsageSupport {

    private static final int RECORD_COUNT = 100;

    public void run() {
        NearCacheConfig nearCacheConfig = createNearCacheConfig()
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setInvalidateOnChange(true)
                .setEvictionConfig(createEvictionConfigWithEntryCountPolicy(RECORD_COUNT * 2));

        ICache<String, Article> cache1 = createCacheWithNearCache(nearCacheConfig);
        ICache<String, Article> cache2 = createCacheWithNearCache(nearCacheConfig);

        String key = generateKeyOwnedBy(getServerInstance());

        cache2.put(key, new Article("foo"));
        printNearCacheStats(cache1, "The cache2.put(key, new Article(\"foo\")) call has no effect on the Near Cache of cache1");

        cache1.get(key);
        printNearCacheStats(cache1, "The first cache1.get(key) call populates the Near Cache of cache1");

        cache2.put(key, new Article("bar"));
        printNearCacheStats(cache1, "The cache2.put(key, new Article(\"bar\") call will invalidate the Near Cache on cache1");

        waitForInvalidationEvents();
        printNearCacheStats(cache1, "The Near Cache of cache1 is empty after the invalidation event has been processed");

        cache1.get(key);
        printNearCacheStats(cache1, "The next cache1.get(key) call populates the Near Cache again");

        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }

    public static void main(String[] args) {
        ClientNearCacheWithInvalidation clientNearCacheUsage = new ClientNearCacheWithInvalidation();
        clientNearCacheUsage.run();
    }
}
