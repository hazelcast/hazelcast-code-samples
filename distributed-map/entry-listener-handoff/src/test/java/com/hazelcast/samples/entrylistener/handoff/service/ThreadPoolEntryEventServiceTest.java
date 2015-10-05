package com.hazelcast.samples.entrylistener.handoff.service;

import com.hazelcast.samples.entrylistener.handoff.service.exceptions.EntryEventServiceException;
import com.hazelcast.samples.entrylistener.handoff.service.listener.BlockingCompletionListener;
import com.hazelcast.samples.entrylistener.handoff.service.listener.LoggingLongRunningThreadListener;
import com.hazelcast.samples.entrylistener.handoff.service.listener.LongRunningThreadListener;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryEventType;
import com.hazelcast.samples.entrylistener.handoff.service.processors.*;
import org.hamcrest.core.IsNot;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Tests the ThreadPoolEntryEventProcessor
 */
public class ThreadPoolEntryEventServiceTest {

    private static final int MAX_SIZE_EXECUTOR_THREAD_POOL = 1;
    private static final int EXECUTOR_QUEUE_SIZE = 1;
    private static final int NUMBER_OF_THREADS_PER_EXECUTOR = 1;

    private EntryEventService<Integer, String, String> entryEventService;
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

        // Empty Factory
        EntryEventTypeProcessorFactory entryEventTypeProcessorFactory = new EntryEventTypeProcessorFactory(entryEventProcessorMap);

        entryEventService = new ThreadPoolEntryEventService<>(MAX_SIZE_EXECUTOR_THREAD_POOL,
                NUMBER_OF_THREADS_PER_EXECUTOR,
                EXECUTOR_QUEUE_SIZE,
                new LoggingLongRunningThreadListener(1,TimeUnit.MINUTES),
                entryEventTypeProcessorFactory);

        entryEventService.process(entryEvent, blockingCompletionListener);
        EntryEventServiceException exception = blockingCompletionListener.getException(1000, TimeUnit.MILLISECONDS);
        assertThat("Wrong exception", exception.getMessage(), equalTo("Could not find EntryEventTypeProcessor for ADDED"));
    }

    @Test
    public void willProcessWithACallableANumberCountOnTheValueString()
            throws EntryEventServiceException, TimeoutException, InterruptedException {

        Map<EntryEventType, Class<? extends EntryEventProcessor>> entryEventProcessorMap = new HashMap<>();
        entryEventProcessorMap.put(EntryEventType.ADDED, WordCountEntryEventProcessor.class);

        EntryEventTypeProcessorFactory entryEventTypeProcessorFactory = new EntryEventTypeProcessorFactory(entryEventProcessorMap);
        entryEventService = new ThreadPoolEntryEventService<>(MAX_SIZE_EXECUTOR_THREAD_POOL,
                NUMBER_OF_THREADS_PER_EXECUTOR,
                EXECUTOR_QUEUE_SIZE,
                new LoggingLongRunningThreadListener(1,TimeUnit.MINUTES),
                entryEventTypeProcessorFactory);

        entryEventService.process(entryEvent, blockingCompletionListener);

        String result = blockingCompletionListener.getResult(1000, TimeUnit.MINUTES);

        assertThat("Null return", result, notNullValue());
        assertThat("Result String is wrong", result, equalTo("1 has 12"));
    }

    @Test
    public void willWarnIfProcessRunsForLongerThanTwoSeconds()
            throws EntryEventServiceException, InterruptedException {

        final CountDownLatch alerted = new CountDownLatch(1);


        Map<EntryEventType, Class<? extends EntryEventProcessor>> entryEventProcessorMap = new HashMap<>();
        entryEventProcessorMap.put(EntryEventType.ADDED, BlockingEntryEventProcessor.class);

        EntryEventTypeProcessorFactory entryEventTypeProcessorFactory = new EntryEventTypeProcessorFactory(entryEventProcessorMap);

        entryEventService = new ThreadPoolEntryEventService<>(MAX_SIZE_EXECUTOR_THREAD_POOL, NUMBER_OF_THREADS_PER_EXECUTOR,
                EXECUTOR_QUEUE_SIZE, new LongRunningThreadListener() {
            @Override
            public void onAlert(Thread t, long elapsedTime) {
                alerted.countDown();
            }

            @Override
            public long getProcessTimeLimit() {
                return 2;
            }

            @Override
            public TimeUnit getProcessTimeLimitTimeUnit() {
                return TimeUnit.SECONDS;
            }
        },entryEventTypeProcessorFactory);

        entryEventService.process(entryEvent, blockingCompletionListener);

        boolean wasAlerted = alerted.await(4, TimeUnit.SECONDS);

        assertThat("Never alerted about overrunning process",wasAlerted,equalTo(true));

    }

    @Test
    public void willWarnAndThenComplete()
            throws EntryEventServiceException, InterruptedException, TimeoutException {

        final AtomicLong alertedCounter = new AtomicLong();


        Map<EntryEventType, Class<? extends EntryEventProcessor>> entryEventProcessorMap = new HashMap<>();
        entryEventProcessorMap.put(EntryEventType.ADDED, SleepingEntryEventProcessor.class);

        EntryEventTypeProcessorFactory entryEventTypeProcessorFactory = new EntryEventTypeProcessorFactory(entryEventProcessorMap);

        entryEventService = new ThreadPoolEntryEventService<>(MAX_SIZE_EXECUTOR_THREAD_POOL, NUMBER_OF_THREADS_PER_EXECUTOR,
                EXECUTOR_QUEUE_SIZE, new LongRunningThreadListener() {
            @Override
            public void onAlert(Thread t, long elapsedTime) {
                alertedCounter.incrementAndGet();
            }

            @Override
            public long getProcessTimeLimit() {
                return 1;
            }

            @Override
            public TimeUnit getProcessTimeLimitTimeUnit() {
                return TimeUnit.SECONDS;
            }
        }, entryEventTypeProcessorFactory);



        entryEventService.process(entryEvent, blockingCompletionListener);


        String result = blockingCompletionListener.getResult(7, TimeUnit.SECONDS);


        assertThat("Never alerted about overrunning process",alertedCounter.get(),IsNot.not(0L));

        assertThat("Null return", result, notNullValue());
        assertThat("Word count size wrong", result, equalTo("12"));


    }

}

