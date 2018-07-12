package com.hazelcast.samples.session.analysis;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryExpiredListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>A listener intended for use with HTTP Sessions, though it's
 * pretty generic so could easily be reused.
 * </p>
 * <p>We are interested in sessions being created (<i>login</i>),
 * removed (<i>logout</i>), updated (use), and expired (system cleanup).
 * We are only interested in the session id, not in this module
 * concerned with the contents of the session attributes.
 * </p>
 */
@SuppressWarnings("rawtypes")
@Slf4j
public class SessionKeyLoggingListener implements EntryAddedListener,
    EntryExpiredListener, EntryRemovedListener, EntryUpdatedListener {

    /* Do the same logging for all events received.
     */
    @Override
    public void entryAdded(EntryEvent entryEvent) {
        this.log(entryEvent);
    }
    @Override
    public void entryExpired(EntryEvent entryEvent) {
        this.log(entryEvent);
    }
    @Override
    public void entryRemoved(EntryEvent entryEvent) {
        this.log(entryEvent);
    }
    @Override
    public void entryUpdated(EntryEvent entryEvent) {
        this.log(entryEvent);
    }

    /**
     * <p>Log the session id and why an event has occurred
     * </p>
     *
     * @param entryEvent
     * @throws Exception
     */
    private void log(EntryEvent entryEvent) {
        log.info("Key '{}' {}", entryEvent.getKey(), entryEvent.getEventType());
    }

}
