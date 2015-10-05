package com.hazelcast.samples.entrylistener.handoff.service.processors;

import com.hazelcast.samples.entrylistener.handoff.service.exceptions.EntryEventServiceException;
import com.hazelcast.core.EntryEvent;

/**
 * An EntryEventProcessor that sleeps.
 */
public class SleepingEntryEventProcessor
        implements EntryEventProcessor<Integer, String, String> {

    @Override
    public String process(EntryEvent<Integer, String> entryEvent)
            throws EntryEventServiceException {
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "12";
    }
}
