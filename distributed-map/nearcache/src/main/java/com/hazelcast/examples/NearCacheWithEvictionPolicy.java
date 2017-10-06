package com.hazelcast.examples;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class NearCacheWithEvictionPolicy extends NearCacheSupport {

    public static void main(String[] args) {
        HazelcastInstance hz = initCluster();
        IMap<Integer, Article> map = hz.getMap("articlesEvictionPolicy");

        for (int i = 1; i <= 100; i++) {
            map.put(i, new Article("foo" + i));
        }
        printNearCacheStats(map, "The put(1..100, article) calls have no effect on the empty Near Cache");

        for (int i = 1; i <= 100; i++) {
            map.get(i);
        }
        printNearCacheStats(map, "The first get(1..100) calls populate the Near Cache");

        for (int i = 1; i <= 100; i++) {
            map.get(i);
        }
        printNearCacheStats(map, "The second get(1..100) calls are served from the Near Cache");

        map.put(101, new Article("bar"));
        printNearCacheStats(map, "The put(101, article) call has no effect on the populated Near Cache");

        map.get(101);
        printNearCacheStats(map, "The first get(101) call triggers the eviction and population of the Near Cache");

        waitForNearCacheEvictionCount(map, 1);
        printNearCacheStats(map, "The Near Cache has been evicted");

        map.get(101);
        printNearCacheStats(map, "The second get(101) call is served from the Near Cache");

        shutdown();
    }
}
