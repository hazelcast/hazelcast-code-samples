package com.hazelcast.hibernate.springhibernate2lc;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hibernate.serialization.Value;
import com.hazelcast.hibernate.springhibernate2lc.persistence.Book;
import com.hazelcast.map.IMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.stream.Stream;

@Component
class SecondLevelCacheVisualizer {

    @Scheduled(fixedDelay = 10000)
    public void cachePeek() {

        HazelcastInstance hazelcastInstance = Stream.concat(Hazelcast.getAllHazelcastInstances().stream(),
                                                            HazelcastClient.getAllHazelcastClients().stream())
                                                    .findAny().orElseThrow(IllegalStateException::new);

        IMap<Object, Value> bookCache = hazelcastInstance.getMap(Book.class.getName());

        System.out.println(LocalTime.now());
        System.out.println("size: " + bookCache.size());
        bookCache.forEach((k, v) -> System.out.println("    " + k.toString() + ":" + v.getValue()));
        System.out.println();
    }
}
