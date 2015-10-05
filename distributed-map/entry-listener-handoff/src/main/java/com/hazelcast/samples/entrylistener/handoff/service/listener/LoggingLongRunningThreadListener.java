package com.hazelcast.samples.entrylistener.handoff.service.listener;

import java.util.concurrent.TimeUnit;

/**
 * A LongRunningThreadListener that writes to System.out with a Warning in event of a long running thread.
 */
public class LoggingLongRunningThreadListener implements LongRunningThreadListener {

    private final long processTimeLimit;
    private final TimeUnit processTimeLimitTimeUnit;

    public LoggingLongRunningThreadListener(long processTimeLimit, TimeUnit processTimeLimitTimeUnit) {
        this.processTimeLimit = processTimeLimit;
        this.processTimeLimitTimeUnit = processTimeLimitTimeUnit;
    }

    @Override
    public void onAlert(Thread t, long elapsedTime) {
        System.out.println("WARNING : " + t + " has been running for " + elapsedTime + "(ms)");
    }

    @Override
    public long getProcessTimeLimit() {
        return this.processTimeLimit;
    }

    @Override
    public TimeUnit getProcessTimeLimitTimeUnit() {
        return this.processTimeLimitTimeUnit;
    }

}
