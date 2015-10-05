package com.hazelcast.samples.entrylistener.handoff.service;

import com.hazelcast.samples.entrylistener.handoff.service.listener.CompletionListener;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

/**
 * Delegates calls to an EntryListener onto an EntryEventService
 */
public class EntryEventServiceDelegate implements EntryListener<Integer,String> {

    private EntryEventService entryEventService;
    private CompletionListener<Integer> completionListener;


    public EntryEventServiceDelegate(EntryEventService entryEventService, CompletionListener<Integer> completionListener) {
        this.entryEventService = entryEventService;
        this.completionListener = completionListener;
    }

    @Override
    public void entryAdded(EntryEvent<Integer, String> entryEvent) {
        entryEventService.process(entryEvent,completionListener);
    }

    @Override
    public void entryRemoved(EntryEvent<Integer, String> entryEvent) {
        entryEventService.process(entryEvent,completionListener);
    }

    @Override
    public void entryUpdated(EntryEvent<Integer, String> entryEvent) {
        entryEventService.process(entryEvent,completionListener);
    }

    @Override
    public void entryEvicted(EntryEvent<Integer, String> entryEvent) {
        entryEventService.process(entryEvent,completionListener);
    }

    @Override
    public void mapEvicted(MapEvent mapEvent) {
        throw new UnsupportedOperationException("MapEvents not supported by EntryEventService");
    }

    @Override
    public void mapCleared(MapEvent mapEvent) {
        throw new UnsupportedOperationException("MapEvents not supported by EntryEventService");
    }
}
