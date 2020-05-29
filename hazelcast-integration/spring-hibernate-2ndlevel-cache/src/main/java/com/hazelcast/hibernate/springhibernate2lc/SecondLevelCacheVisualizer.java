package com.hazelcast.hibernate.springhibernate2lc;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
class SecondLevelCacheVisualizer {

    private final HazelcastInstance instance;

    public SecondLevelCacheVisualizer(HazelcastInstance instance) {
        this.instance = instance;
    }

    @Scheduled(fixedDelay = 10000)
    public void cachePeek() {

        IMap<Object, com.hazelcast.hibernate.serialization.Value> fooCache = instance.getMap("com.hazelcast.hibernate.springhibernate2lc.persistence.Book");

        System.out.println(LocalTime.now());
        System.out.println("size: " + fooCache.size());
        fooCache.forEach((k, v) -> System.out.println("    " + k.toString() + ":" + v.getValue()) );
        System.out.println();
    }
}
