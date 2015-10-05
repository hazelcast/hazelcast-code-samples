package com.hazelcast.samples.entrylistener.handoff.service;

import com.hazelcast.samples.entrylistener.handoff.service.exceptions.EntryEventServiceException;
import com.hazelcast.samples.entrylistener.handoff.service.listener.CompletionListener;
import com.hazelcast.samples.entrylistener.handoff.service.listener.LongRunningThreadListener;
import com.hazelcast.samples.entrylistener.handoff.service.processors.EntryEventProcessor;
import com.hazelcast.samples.entrylistener.handoff.service.processors.EntryEventTypeProcessorFactory;
import com.hazelcast.core.EntryEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * EntryEventProcessors should be used to hand off EntryEvent processing from the Hazelcast Event Threads.  It is generally
 * considered bad practice to have potentially blocking and/or long running code hanging off the Hazelcast Event Thread.
 * <p/>
 * ThreadPoolEntryEventProcessor provides a striped set of ThreadPoolExecutors that guarantees execution order by key.
 * <ul>
 * <li>A Warning Service to inform of long running EntryEventTypeProcessors</li>
 * <li>CompletionService callback to inform of failed/completed processes</li>
 * <li>Getter to retrieve the striped queues of waiting EntryEvents</li>
 * </ul>
 */
public class ThreadPoolEntryEventService<K, V, R> implements EntryEventService<K,V,R>{

    public static final TimeUnit EXECUTOR_THREAD_TTL_TIMEUNIT = TimeUnit.HOURS;
    public static final long EXECUTOR_THREAD_TTL = 1;

    private final int numberOfExecutors;
    private final LongRunningThreadListener longRunningThreadListener;
    private final EntryEventTypeProcessorFactory entryEventTypeProcessorFactory;

    // List of the CompletionServices that are backed by an ExecutorService
    private List<ThreadPoolExecutor> executorList = new ArrayList();

    public ThreadPoolEntryEventService(int numberOfExecutors,
                                       int numberOfThreadsPerExecutor,
                                       int executorQueueCapacity,
                                       LongRunningThreadListener longRunningThreadListener,
                                       EntryEventTypeProcessorFactory entryEventTypeProcessorFactory) {

        this.numberOfExecutors = numberOfExecutors;
        this.longRunningThreadListener = longRunningThreadListener;
        this.entryEventTypeProcessorFactory = entryEventTypeProcessorFactory;

        setUpExecutors(numberOfExecutors, numberOfThreadsPerExecutor, executorQueueCapacity);
    }

    private void setUpExecutors(int numberOfExecutors, int numberOfThreadsPerExecutor, int queueCapacity) {
        for (int arrayIndex = 0; arrayIndex < numberOfExecutors; arrayIndex++) {
            ThreadPoolExecutor threadPoolExecutor = new TimedThreadPoolExector(numberOfThreadsPerExecutor,
                    numberOfThreadsPerExecutor, EXECUTOR_THREAD_TTL, EXECUTOR_THREAD_TTL_TIMEUNIT,
                    new ArrayBlockingQueue(queueCapacity), longRunningThreadListener);

            executorList.add(arrayIndex, threadPoolExecutor);
        }
    }

    /**
     * Process the EntryEvent using the provided EntryEventCallable.
     * Throws EntryEventProcessorException if the queue is full.
     *
     * @param entryEvent
     * @return R The result of the EntryEventCallable
     * @throws EntryEventServiceException
     */
    @Override
    public void process(EntryEvent<K, V> entryEvent, CompletionListener<R> completionListener) {

        ThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor(entryEvent);

        EntryEventProcessor<K, V, R> entryEventProcessor;

        try {
            entryEventProcessor = entryEventTypeProcessorFactory.getEntryEventTypeProcessor(entryEvent.getEventType());
        } catch (EntryEventServiceException e) {
            completionListener.onException(e);
            return;
        }

        CallableEntryEventTypeProcessor<K, V, R> callableEntryEventTypeProcessor = new CallableEntryEventTypeProcessor<>(
                completionListener, entryEvent, entryEventProcessor);

        submitEntryEventProcessor(completionListener, threadPoolExecutor, callableEntryEventTypeProcessor);

    }

    /**
     * Returns the Queue that a given key would be offered to. The EntryEventTypeProcessor representing this key
     * may or may not be present.
     *
     * @param key
     * @return BlockingQueue
     */
    public BlockingQueue getQueueForKey(K key) {
        return executorList.get(getKeyIndex(key)).getQueue();
    }

    private void submitEntryEventProcessor(CompletionListener<R> completionListener, ThreadPoolExecutor threadPoolExecutor,
                                           CallableEntryEventTypeProcessor<K, V, R> callableEntryEventTypeProcessor) {
        try {
            threadPoolExecutor.submit(callableEntryEventTypeProcessor);
        } catch (RejectedExecutionException e) {
            completionListener.onException(new EntryEventServiceException(e));
        }
    }

    private ThreadPoolExecutor getThreadPoolExecutor(EntryEvent<K, V> entryEvent) {
        int executorListIndex = getKeyIndex(entryEvent.getKey());
        return executorList.get(executorListIndex);
    }

    private int getKeyIndex(K key) {
        return Math.abs(key.hashCode() % numberOfExecutors);
    }

    private class TimedThreadPoolExector
            extends ThreadPoolExecutor {

        private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
        private final LongRunningThreadListener longRunningThreadListener;
        private final Map<Runnable,ScheduledFuture> runningTasksMap = new ConcurrentHashMap<Runnable,ScheduledFuture>();


        public TimedThreadPoolExector(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                      BlockingQueue<Runnable> workQueue, LongRunningThreadListener longRunningThreadListener) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
            this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(corePoolSize);
            this.longRunningThreadListener = longRunningThreadListener;

        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {

            Date timeNow = new Date();
            long processTimeLimit = longRunningThreadListener.getProcessTimeLimit();
            TimeUnit processTimeLimitTimeUnit = longRunningThreadListener.getProcessTimeLimitTimeUnit();
            ScheduledFuture<?> scheduledFuture = scheduledThreadPoolExecutor
                    .scheduleAtFixedRate(new TimedTask(t, timeNow.getTime(), longRunningThreadListener),
                            processTimeLimit, processTimeLimit, processTimeLimitTimeUnit);

            runningTasksMap.put(r,scheduledFuture);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            ScheduledFuture scheduledFuture = runningTasksMap.get(r);
            scheduledFuture.cancel(true);
        }

    }

    private class TimedTask
            implements Runnable {

        private final long startTime;
        private final Thread thread;
        private final LongRunningThreadListener longRunningThreadListener;

        public TimedTask(Thread t, long time, LongRunningThreadListener longRunningThreadListener) {
            this.thread = t;
            this.startTime = time;
            this.longRunningThreadListener = longRunningThreadListener;
        }

        @Override
        public void run() {
            long timeNow = new Date().getTime();
            long elapsedTime = timeNow - startTime;
            longRunningThreadListener.onAlert(thread, elapsedTime);
            if (Thread.interrupted()){
                System.out.println("cancelling");
                return;
            }
        }

    }

}
