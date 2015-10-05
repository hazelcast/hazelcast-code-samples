package com.hazelcast.samples.entrylistener.handoff.service.processors;

import com.hazelcast.samples.entrylistener.handoff.service.exceptions.EntryEventServiceException;
import com.hazelcast.core.EntryEventType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides EntryEventProcessor keyed by EventType.  For each call generates a new EntryEventProcessor instance.
 */
public class EntryEventTypeProcessorFactory<K,V,R> {

    private Map<EntryEventType,Class<? extends EntryEventProcessor<K,V,R>>> entryEventTypeProcessorMap = new ConcurrentHashMap<>();

    public EntryEventTypeProcessorFactory(Map<EntryEventType, Class<? extends EntryEventProcessor<K, V, R>>> entryEventTypeProcessorMap) {
        this.entryEventTypeProcessorMap = entryEventTypeProcessorMap;
    }

    public EntryEventProcessor<K, V, R> getEntryEventTypeProcessor(EntryEventType entryEventType) throws
            EntryEventServiceException {

        Class<? extends EntryEventProcessor<K, V, R>> entryEventTypeProcessorClass = entryEventTypeProcessorMap.get(entryEventType);

        if (entryEventTypeProcessorClass == null) throw
                new EntryEventServiceException("Could not find EntryEventTypeProcessor for " + entryEventType.toString());

        return createEntryEventTypeProcessor(entryEventTypeProcessorClass);

    }

    private EntryEventProcessor<K, V, R> createEntryEventTypeProcessor(Class<? extends EntryEventProcessor<K, V, R>> entryEventTypeProcessorClass)
            throws EntryEventServiceException {

        EntryEventProcessor<K, V, R> entryEventProcessor;

        try {
            entryEventProcessor = entryEventTypeProcessorClass.newInstance();
        } catch (InstantiationException e) {
            throw new EntryEventServiceException(e);
        } catch (IllegalAccessException e) {
            throw new EntryEventServiceException(e);
        }

        return entryEventProcessor;
    }


}
