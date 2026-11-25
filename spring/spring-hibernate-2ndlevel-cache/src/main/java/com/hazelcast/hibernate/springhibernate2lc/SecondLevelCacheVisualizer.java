package com.hazelcast.hibernate.springhibernate2lc;

import com.hazelcast.hibernate.springhibernate2lc.persistence.Book;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.time.LocalTime;

@Component
class SecondLevelCacheVisualizer {

    private final CacheManager cacheManager;

    SecondLevelCacheVisualizer(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedDelay = 10000)
    public void cachePeek() {
        Cache<Object, Object> bookCache = cacheManager.getCache(Book.class.getName());
        System.out.println(LocalTime.now());
        for (Cache.Entry<Object, Object> entry : bookCache) {
            System.out.println("    " + entry.getKey() + ":" + entry.getValue());
        }
        System.out.println();
    }
}
