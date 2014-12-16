package com.hazelcast.examples.declarative;

import javax.cache.configuration.Factory;

/**
 * factory for LoggingEntryListener
 */
public class LoggingEntryListenerFactory implements Factory<LoggingEntryListener> {

    @Override
    public LoggingEntryListener create() {
        return new LoggingEntryListener();
    }
}
