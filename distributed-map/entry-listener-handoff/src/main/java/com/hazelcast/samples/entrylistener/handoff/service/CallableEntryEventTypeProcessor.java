package com.hazelcast.samples.entrylistener.handoff.service;

import com.hazelcast.samples.entrylistener.handoff.service.exceptions.EntryEventServiceException;
import com.hazelcast.samples.entrylistener.handoff.service.listener.CompletionListener;
import com.hazelcast.samples.entrylistener.handoff.service.processors.EntryEventProcessor;
import com.hazelcast.core.EntryEvent;

import java.util.concurrent.Callable;

/**
 * Wraps an EntryEventTypeProcessor in a Callable and informs the passed CompletionListener of the Callable result or an
 * exception.
 * <p>
 * The Callable returns a result that should be consumed to any internal EntryEventProcessor.  Results are passed on via
 * the CompletionListener.
 * </p>
 */
public class CallableEntryEventTypeProcessor<K,V,R> implements Callable<R> {

    private CompletionListener<R> completionListener;
    private EntryEventProcessor<K,V,R> entryEventProcessor;
    private EntryEvent entryEvent;

    public CallableEntryEventTypeProcessor(CompletionListener<R> completionListener,
                                           EntryEvent entryEvent,
                                           EntryEventProcessor<K, V, R> entryEventProcessor) {
        this.completionListener = completionListener;
        this.entryEvent = entryEvent;
        this.entryEventProcessor = entryEventProcessor;
    }


    @Override
    public R call()
            throws Exception {
        R result = null;
        try {
            result = entryEventProcessor.process(entryEvent);
            completionListener.onCompletion(result);
        } catch (EntryEventServiceException e) {
            completionListener.onException(e);
        }
        return result;
    }
}