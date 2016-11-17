package com.hazelcast.examples.nearcache;

import com.hazelcast.cache.ICache;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.examples.Article;

public class ClientNearCacheWithTTL extends ClientNearCacheUsageSupport {

    private static final int RECORD_COUNT = 100;
    private static final int TIME_TO_LIVE_SECONDS = 1;

    public void run() {
        NearCacheConfig nearCacheConfig = createNearCacheConfig()
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setCacheLocalEntries(true)
                .setInvalidateOnChange(false)
                .setTimeToLiveSeconds(TIME_TO_LIVE_SECONDS)
                .setEvictionConfig(createEvictionConfigWithEntryCountPolicy(RECORD_COUNT * 2));

        ICache<Integer, Article> cache = createCacheWithNearCache(nearCacheConfig);

        cache.put(1, new Article("foo"));
        printNearCacheStats(cache, "The put(1, article) call has no effect on the empty Near Cache");

        cache.get(1);
        printNearCacheStats(cache, "The first get(1) call populates the Near Cache");

        waitForExpirationTask(TIME_TO_LIVE_SECONDS);
        printNearCacheStats(cache, "We've waited for the time-to-live-seconds, so the Near Cache entry is expired.");

        cache.get(1);
        printNearCacheStats(cache, "The next get(1) call is fetching the value again from the cache");

        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }

    public static void main(String[] args) {
        ClientNearCacheWithTTL clientNearCacheUsage = new ClientNearCacheWithTTL();
        clientNearCacheUsage.run();
    }
}
