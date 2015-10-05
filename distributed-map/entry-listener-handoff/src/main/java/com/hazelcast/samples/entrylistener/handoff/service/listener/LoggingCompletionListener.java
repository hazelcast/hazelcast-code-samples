package com.hazelcast.samples.entrylistener.handoff.service.listener;

import com.hazelcast.samples.entrylistener.handoff.service.exceptions.EntryEventServiceException;

/**
 * A Completion Listener that sends results and exceptions to System.out
 */
public class LoggingCompletionListener<R> implements CompletionListener<R> {

    @Override
    public void onCompletion(R result) {
        System.out.println("onCompletion " + result);
    }

    @Override
    public void onException(EntryEventServiceException e) {
        System.out.println(e);
    }

}
