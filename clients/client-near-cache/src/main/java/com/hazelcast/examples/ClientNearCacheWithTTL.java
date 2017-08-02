package com.hazelcast.examples;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import static com.hazelcast.examples.helper.CommonUtils.sleepSeconds;

public class ClientNearCacheWithTTL extends NearCacheClientSupport {

    public static void main(String[] args) {
        HazelcastInstance hz = initCluster();
        IMap<Integer, Article> map = hz.getMap("articlesTTL");

        map.put(1, new Article("foo"));
        printNearCacheStats(map, "The put(1, article) call has no effect on the empty Near Cache");

        map.get(1);
        printNearCacheStats(map, "The first get(1) call populates the Near Cache");

        map.get(1);
        printNearCacheStats(map, "The second get(1) call is served from the Near Cache");

        sleepSeconds(2);
        System.out.println("We've waited for the time-to-live-seconds, so the Near Cache entry is expired.");

        map.get(1);
        printNearCacheStats(map, "The third get(1) call is fetching the value again from the map");

        shutdown();
    }
}
