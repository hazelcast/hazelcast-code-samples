package com.hazelcast.spring.jcache;

import javax.cache.annotation.CacheResult;

public interface IDummyBean {

    @CacheResult(cacheName = "city")
    String getCity();
}