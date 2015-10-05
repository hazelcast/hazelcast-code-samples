package com.hazelcast.samples.entrylistener.handoff.service;

import com.hazelcast.samples.entrylistener.handoff.service.exceptions.EntryEventServiceException;
import com.hazelcast.samples.entrylistener.handoff.service.listener.BlockingCompletionListener;
import com.hazelcast.samples.entrylistener.handoff.service.processors.EntryEventProcessor;
import com.hazelcast.samples.entrylistener.handoff.service.processors.EntryEventTypeProcessorFactory;
import com.hazelcast.samples.entrylistener.handoff.service.processors.WordCountEntryEventProcessor;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryEventType;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

/**
 * Tests the SingleThreadEntryEventProcessor
 */
public class SimpleEntryEventServiceTest {

    private EntryEvent entryEvent;
    private BlockingCompletionListener<String> blockingCompletionListener;

    @Before
    public void startUp(){
        blockingCompletionListener = new BlockingCompletionListener<>();
        entryEvent = new EntryEvent("", null, EntryEventType.ADDED.getType(), 1, "Spurs are on their way to Wembley, Tottenhams gonna do it again");
    }

    @Test
    public void willFailWithoutAProcessor()
            throws EntryEventServiceException {

        Map<EntryEventType, Class<? extends EntryEventProcessor>> entryEventProcessorMap = new HashMap<>();
        EntryEventTypeProcessorFactory entryEventTypeProcessorFactory = new EntryEventTypeProcessorFactory(entryEventProcessorMap);
        EntryEventService entryEventService = new SimpleEntryEventService(entryEventTypeProcessorFactory);


        entryEventService.process(entryEvent, blockingCompletionListener);
        EntryEventServiceException exception = blockingCompletionListener.getException(1000, TimeUnit.MILLISECONDS);
        assertThat("Wrong exception",exception.getCause().getMessage(), equalTo("Could not find EntryEventTypeProcessor for ADDED"));
    }

    @Test
    public void willProcessWithACallableANumberCountOnTheValueString()
            throws EntryEventServiceException, TimeoutException, InterruptedException {

        Map<EntryEventType, Class<? extends EntryEventProcessor>> entryEventProcessorMap = new HashMap<>();
        entryEventProcessorMap.put(EntryEventType.ADDED, WordCountEntryEventProcessor.class);

        EntryEventTypeProcessorFactory entryEventTypeProcessorFactory = new EntryEventTypeProcessorFactory(entryEventProcessorMap);

        EntryEventService entryEventService = new SimpleEntryEventService(entryEventTypeProcessorFactory);

        entryEventService.process(entryEvent, blockingCompletionListener);

        String result = blockingCompletionListener.getResult(1000, TimeUnit.MILLISECONDS);

        assertThat("Null return", result, notNullValue());
        assertThat("Result string is wrong", result, equalTo("1 has 12"));
    }

}
