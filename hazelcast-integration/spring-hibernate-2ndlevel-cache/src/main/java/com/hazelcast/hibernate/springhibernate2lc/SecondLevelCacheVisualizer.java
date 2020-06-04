package com.hazelcast.hibernate.springhibernate2lc;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.hibernate.springhibernate2lc.persistence.Book;
import com.hazelcast.map.IMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
class SecondLevelCacheVisualizer {

    @Scheduled(fixedDelay = 10000)
    public void cachePeek() {

        IMap<Object, com.hazelcast.hibernate.serialization.Value> fooCache = Hazelcast.getAllHazelcastInstances()
          .stream()
          .findAny().orElseThrow(IllegalStateException::new)
          .getMap(Book.class.getName());

        System.out.println(LocalTime.now());
        System.out.println("size: " + fooCache.size());
        fooCache.forEach((k, v) -> System.out.println("    " + k.toString() + ":" + v.getValue()));
        System.out.println();
    }
}
