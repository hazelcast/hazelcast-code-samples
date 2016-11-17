package com.hazelcast.examples.nearcache;

import com.hazelcast.cache.ICache;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.examples.Article;

public class ClientNearCacheWithMemoryFormatObject extends ClientNearCacheUsageSupport {

    private static final int RECORD_COUNT = 100;

    public void run() {
        NearCacheConfig nearCacheConfig = createNearCacheConfig()
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setCacheLocalEntries(true)
                .setInvalidateOnChange(false)
                .setEvictionConfig(createEvictionConfigWithEntryCountPolicy(RECORD_COUNT * 2));

        ICache<Integer, Article> cache = createCacheWithNearCache(nearCacheConfig);

        Article article = new Article("foo");
        cache.put(1, article);

        // the first get() will populate the Near Cache
        Article firstGet = cache.get(1);
        // the second and third get() will be served from the Near Cache
        Article secondGet = cache.get(1);
        Article thirdGet = cache.get(1);

        printNearCacheStats(cache);

        System.out.println("Since we use in-memory format BINARY, the article instances from the Near Cache will be different.");
        System.out.println("Compare first and second article instance: " + (firstGet == secondGet));
        System.out.println("Compare second and third article instance: " + (secondGet == thirdGet));

        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }

    public static void main(String[] args) {
        ClientNearCacheWithMemoryFormatObject clientNearCacheUsage = new ClientNearCacheWithMemoryFormatObject();
        clientNearCacheUsage.run();
    }
}
