package com.hazelcast.spring.jcache;

import javax.cache.annotation.CacheResult;

interface IDummyBean {

    @CacheResult(cacheName = "city")
    String getCity();
}
