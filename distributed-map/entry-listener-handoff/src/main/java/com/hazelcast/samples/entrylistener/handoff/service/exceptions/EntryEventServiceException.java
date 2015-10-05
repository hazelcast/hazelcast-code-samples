package com.hazelcast.samples.entrylistener.handoff.service.exceptions;

/**
 * Created by dbrimley on 03/02/15.
 */
public class EntryEventServiceException
        extends Exception {

    public EntryEventServiceException() {
        super();
    }

    public EntryEventServiceException(String message) {
        super(message);
    }

    public EntryEventServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntryEventServiceException(Throwable cause) {
        super(cause);
    }

    protected EntryEventServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
