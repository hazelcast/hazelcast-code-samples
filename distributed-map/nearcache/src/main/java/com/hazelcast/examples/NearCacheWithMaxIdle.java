package com.hazelcast.examples;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import static com.hazelcast.examples.helper.CommonUtils.sleepMillis;
import static com.hazelcast.examples.helper.CommonUtils.sleepSeconds;

public class NearCacheWithMaxIdle extends NearCacheSupport {

    public static void main(String[] args) {
        HazelcastInstance hz = initCluster();
        IMap<Integer, Article> map = hz.getMap("articlesMaxIdle");

        map.put(1, new Article("foo"));
        printNearCacheStats(map, "The put(1, article) call has no effect on the empty Near Cache");

        map.get(1);
        printNearCacheStats(map, "The first get(1) call populates the Near Cache");

        // with this short sleep time, the Near Cache entry should not expire
        for (int i = 0; i < 20; i++) {
            map.get(1);
            sleepMillis(100);
        }
        printNearCacheStats(map, "We have called get(1) every 100 ms, so the Near cache entry could not expire");

        sleepSeconds(2);
        System.out.println("We've waited for max-idle-seconds, so the Near Cache entry is expired.");

        map.get(1);
        printNearCacheStats(map, "The next get(1) call is fetching the value again from the map");

        shutdown();
    }
}
