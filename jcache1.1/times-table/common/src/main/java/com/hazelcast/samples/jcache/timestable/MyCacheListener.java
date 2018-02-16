package com.hazelcast.samples.jcache.timestable;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>A cache listener that logs events from the
 * selected types. Other event types are ignored.
 * </p>
 */
@Slf4j
public class MyCacheListener<K, V> implements CacheEntryCreatedListener<K, V>, CacheEntryUpdatedListener<K, V> {

    /**
     * <p>Iterate through the create events, one each to the logger
     * </p>
     */
    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends K, ? extends V>> iterable)
            throws CacheEntryListenerException {
        iterable.iterator().forEachRemaining(cacheEntryEvent -> this.log(cacheEntryEvent));
    }

    /**
     * <p>Iterate through the update events, one each to the logger
     * </p>
     */
    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends K, ? extends V>> iterable)
            throws CacheEntryListenerException {
        iterable.iterator().forEachRemaining(cacheEntryEvent -> this.log(cacheEntryEvent));
    }

    /**
     * <p>Log a cache event
     * </p>
     *
     * @param cacheEntryEvent
     */
    private void log(CacheEntryEvent<? extends K, ? extends V> cacheEntryEvent) {
        log.info("Cache entry {} K=='{}' V=='{}'",
                cacheEntryEvent.getEventType(),
                cacheEntryEvent.getKey(),
                cacheEntryEvent.getValue());
    }

}
