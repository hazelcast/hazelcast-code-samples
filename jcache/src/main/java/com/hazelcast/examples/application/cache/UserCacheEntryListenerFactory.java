package com.hazelcast.examples.application.cache;

import com.hazelcast.examples.application.model.User;

import javax.cache.configuration.Factory;
import javax.cache.event.CacheEntryListener;

public class UserCacheEntryListenerFactory implements Factory<CacheEntryListener<Integer, User>> {

    @Override
    public CacheEntryListener<Integer, User> create() {
        // just create a new listener instance
        return new UserCacheEntryListener();
    }
}
