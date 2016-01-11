package com.hazelcast.examples.declarative;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;

/**
 * log all events to system out
 */
public class LoggingEntryListener implements CacheEntryCreatedListener,
        CacheEntryUpdatedListener,
        CacheEntryRemovedListener,
        CacheEntryExpiredListener {

    @Override
    public void onCreated(Iterable iterable) throws CacheEntryListenerException {
        onEvent(iterable);
    }

    @Override
    public void onExpired(Iterable iterable) throws CacheEntryListenerException {
        onEvent(iterable);
    }

    @Override
    public void onRemoved(Iterable iterable) throws CacheEntryListenerException {
        onEvent(iterable);
    }

    @Override
    public void onUpdated(Iterable iterable) throws CacheEntryListenerException {
        onEvent(iterable);
    }

    private void onEvent(Iterable iterable) throws CacheEntryListenerException {
        for (Object o : iterable) {
            System.out.println(o);
        }
    }
}
