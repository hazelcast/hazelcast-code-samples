package com.hazelcast.samples.entrylistener.handoff.service.listener;

import java.util.concurrent.TimeUnit;


/**
 * The LongRunningThreadListener is a callback mechanism used by the ThreadPoolEntryEventService.
 * <p>
 * It is passed in a Constructor and uses the getProcessTimeLimt() and getProcessTimeLimitTimeUnit() methods
 * to create a scheduled task that checks if the EntryEVentProcessor is still running.  If it is over this time
 * the ThreadPoolEntryEventService will call the onAlert() method passed the long running Thread and the elapsedTime.
 *
 */
public interface LongRunningThreadListener {

    public void onAlert(Thread t, long elapsedTime);

    public long getProcessTimeLimit();

    public TimeUnit getProcessTimeLimitTimeUnit();
}
