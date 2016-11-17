package com.hazelcast.examples.nearcache;

import com.hazelcast.cache.ICache;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.examples.Article;

import static com.hazelcast.examples.helper.HazelcastUtils.generateKeyOwnedBy;

public class ClientNearCacheWithLocalUpdatePolicy extends ClientNearCacheUsageSupport {

    private static final int RECORD_COUNT = 100;

    public void run() {
        NearCacheConfig nearCacheConfig = createNearCacheConfig()
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setInvalidateOnChange(false)
                .setLocalUpdatePolicy(NearCacheConfig.LocalUpdatePolicy.CACHE)
                .setEvictionConfig(createEvictionConfigWithEntryCountPolicy(RECORD_COUNT * 2));

        ICache<String, Article> cache1 = createCacheWithNearCache(nearCacheConfig);
        ICache<String, Article> cache2 = createCacheWithNearCache(nearCacheConfig);

        String key = generateKeyOwnedBy(getServerInstance());

        cache1.put(key, new Article("foo"));
        printNearCacheStats(cache1, "The cache1.put(key, new Article(\"foo\")) call will populate the Near Cache of cache1, ...");
        printNearCacheStats(cache2, "..., but has no effect on the Near Cache of cache2");

        cache1.get(key);
        printNearCacheStats(cache1, "The first cache1.get(key) call be served by the Near Cache of cache1");

        cache2.get(key);
        printNearCacheStats(cache2, "The first cache2.get(key) call populates the Near Cache of cache2");

        cache1.put(key, new Article("bar"));
        printNearCacheStats(cache1, "The cache1.put(key, new Article(\"bar\") call will update the Near Cache of cache1, ...");
        printNearCacheStats(cache2, "..., but has no effect on the Near Cache of cache2");

        Article article1 = cache1.get(key);
        printNearCacheStats(cache1, "The second cache1.get(key) call will be served by the Near Cache of cache1");

        Article article2 = cache2.get(key);
        printNearCacheStats(cache2, "The second cache2.get(key) call will be served by the Near Cache of cache2");

        System.out.printf("The retrieved articles are not the same: %s vs. %s%n", article1.getName(), article2.getName());

        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }

    public static void main(String[] args) {
        ClientNearCacheWithLocalUpdatePolicy clientNearCacheUsage = new ClientNearCacheWithLocalUpdatePolicy();
        clientNearCacheUsage.run();
    }
}
