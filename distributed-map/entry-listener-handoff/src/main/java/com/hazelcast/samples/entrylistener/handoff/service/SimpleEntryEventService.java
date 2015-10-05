package com.hazelcast.samples.entrylistener.handoff.service;

import com.hazelcast.samples.entrylistener.handoff.service.exceptions.EntryEventServiceException;
import com.hazelcast.samples.entrylistener.handoff.service.listener.CompletionListener;
import com.hazelcast.samples.entrylistener.handoff.service.processors.EntryEventProcessor;
import com.hazelcast.samples.entrylistener.handoff.service.processors.EntryEventTypeProcessorFactory;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryEventType;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * An EntryEventProcessor that runs on the calling thread.
 */
public class SimpleEntryEventService<K, V, R> implements EntryEventService<K,V,R> {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final EntryEventTypeProcessorFactory entryEventTypeProcessorFactory;

    public SimpleEntryEventService(EntryEventTypeProcessorFactory entryEventTypeProcessorFactory) {
        this.entryEventTypeProcessorFactory = entryEventTypeProcessorFactory;
    }

    @Override
    public void process(EntryEvent<K, V> entryEvent, CompletionListener<R> completionListener) {
        try {
            EntryEventType entryEventType = entryEvent.getEventType();
            EntryEventProcessor<K, V, R> entryEventProcessor = entryEventTypeProcessorFactory.getEntryEventTypeProcessor(entryEventType);

            CallableEntryEventTypeProcessor<K, V, R> callableEntryEventTypeProcessor = new CallableEntryEventTypeProcessor<>(
                    completionListener, entryEvent, entryEventProcessor);

            executor.submit(callableEntryEventTypeProcessor);

        } catch (Exception e) {
            completionListener.onException(new EntryEventServiceException(e));
        }
    }

}
