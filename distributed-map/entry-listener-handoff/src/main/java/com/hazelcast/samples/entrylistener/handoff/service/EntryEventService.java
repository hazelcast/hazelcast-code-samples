package com.hazelcast.samples.entrylistener.handoff.service;

import com.hazelcast.samples.entrylistener.handoff.service.listener.CompletionListener;
import com.hazelcast.core.EntryEvent;

/**
 * Framework to provide encapsulation of EntryEvent processing.  Can be used to provide offloading of processing from
 * Hazelcast event threads.  EntryEvents are passed off to the EntryEventService via the process method.
 * <p>
 * The EntryEventService then selects the appropriate EntryEventProcessor.
 * <p>
 * If an EntryEvent is passed for processing and a EntryEventTypeProcessor is not found for that EntryEventType
 * then a EntryEventServiceException is passed to the caller via the CompletionListener.onException()
 */
public interface EntryEventService<K,V,R> {

    void process(EntryEvent<K,V> entryEvent, CompletionListener<R> completionListener);

}
