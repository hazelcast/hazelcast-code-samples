package com.hazelcast.samples.entrylistener.handoff.service.listener;

import com.hazelcast.samples.entrylistener.handoff.service.exceptions.EntryEventServiceException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Completion Listener that can optionally block for an Exception or Result, or wait until a time limit has been reached.
 */
public class BlockingCompletionListener<R>
        implements CompletionListener<R> {

    private CountDownLatch latch = new CountDownLatch(1);
    private R result;
    private EntryEventServiceException exception;

    @Override
    public void onCompletion(R result) {
        this.result = result;
        latch.countDown();
    }

    @Override
    public void onException(EntryEventServiceException e) {
        this.exception = e;
        latch.countDown();
    }

    public R getResult(long timeout, TimeUnit timeUnit)
            throws InterruptedException, TimeoutException {

        boolean latchTriggered = latch.await(timeout, timeUnit);

        if (latchTriggered) {
            return result;
        }

        throw new TimeoutException("Time out waiting for completion");
    }

    public R getResult()
            throws InterruptedException {
        latch.await();
        return result;
    }

    public EntryEventServiceException getException(long timeout, TimeUnit timeUnit) {

        boolean latchTriggered = false;
        try {
            latchTriggered = latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return new EntryEventServiceException(e);
        }

        if (latchTriggered) {
            return exception;
        }

        return new EntryEventServiceException(new TimeoutException("Time out waiting for completion"));
    }

    public EntryEventServiceException getException() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            return new EntryEventServiceException(e);
        }
        return new EntryEventServiceException(exception);
    }

}
