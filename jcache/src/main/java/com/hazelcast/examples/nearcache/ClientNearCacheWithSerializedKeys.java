package com.hazelcast.examples.nearcache;

import com.hazelcast.cache.ICache;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.examples.Article;
import com.hazelcast.examples.ArticleKey;

public class ClientNearCacheWithSerializedKeys extends ClientNearCacheUsageSupport {

    private static final int RECORD_COUNT = 100;

    public void run() {
        NearCacheConfig nearCacheConfig1 = createNearCacheConfig()
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setCacheLocalEntries(true)
                .setInvalidateOnChange(false)
                .setEvictionConfig(createEvictionConfigWithEntryCountPolicy(RECORD_COUNT * 2));

        NearCacheConfig nearCacheConfig2 = createNearCacheConfig()
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setSerializeKeys(true)
                .setCacheLocalEntries(true)
                .setInvalidateOnChange(false)
                .setEvictionConfig(createEvictionConfigWithEntryCountPolicy(RECORD_COUNT * 2));

        ICache<ArticleKey, Article> cache1 = createCacheWithNearCache(nearCacheConfig1);
        ICache<ArticleKey, Article> cache2 = createCacheWithNearCache(nearCacheConfig2);

        System.out.println("Near Cache of cache 1 uses serialized keys: " + nearCacheConfig1.isSerializeKeys());
        System.out.println("Near Cache of cache 2 uses serialized keys: " + nearCacheConfig2.isSerializeKeys());

        ArticleKey key1 = new ArticleKey(1);
        ArticleKey key2 = new ArticleKey(2);
        Article article = new Article("foo");

        System.out.println("\nPopulate the data structure (both keys are serialized, since it's a remote operation)...");
        cache1.put(key1, article);
        cache2.put(key2, article);

        System.out.println("\nThe first get() will populate the Near Cache (again a remote operation for both keys)...");
        cache1.get(key1);
        cache2.get(key2);

        System.out.println("\nThe second get() will be served from the Near Cache (key 2 is serialized for the local lookup)...");
        cache1.get(key1);
        cache2.get(key2);

        System.out.println("\nThe Near Cache statistics are the same (the key serialization is transparent to the user)...");
        System.out.println("Near Cache from cache 1: " + cache1.getLocalCacheStatistics().getNearCacheStatistics());
        System.out.println("Near Cache from cache 2: " + cache2.getLocalCacheStatistics().getNearCacheStatistics());
        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }

    public static void main(String[] args) {
        ClientNearCacheWithSerializedKeys clientNearCacheUsage = new ClientNearCacheWithSerializedKeys();
        clientNearCacheUsage.run();
    }
}
