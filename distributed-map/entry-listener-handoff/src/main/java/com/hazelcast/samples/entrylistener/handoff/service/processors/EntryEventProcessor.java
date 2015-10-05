package com.hazelcast.samples.entrylistener.handoff.service.processors;

import com.hazelcast.samples.entrylistener.handoff.service.exceptions.EntryEventServiceException;
import com.hazelcast.core.EntryEvent;

/**
 * Created by dbrimley on 11/02/15.
 */
public interface EntryEventProcessor<K, V, R> {

    R process(EntryEvent<K,V> entryEvent) throws EntryEventServiceException;

}
