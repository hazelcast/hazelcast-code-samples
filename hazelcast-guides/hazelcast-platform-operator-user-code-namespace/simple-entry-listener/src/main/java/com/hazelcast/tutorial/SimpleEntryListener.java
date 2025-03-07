package com.hazelcast.tutorial;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

public class SimpleEntryListener implements EntryAddedListener<String, HazelcastJsonValue>,
        EntryUpdatedListener<String, HazelcastJsonValue>,
        EntryRemovedListener<String, HazelcastJsonValue> {

    @Override
    public void entryAdded(EntryEvent<String, HazelcastJsonValue> event) {
        System.out.println("Entry Added:" + event);
    }

    @Override
    public void entryRemoved(EntryEvent<String, HazelcastJsonValue> event) {
        System.out.println("Entry Removed:" + event);
    }

    @Override
    public void entryUpdated(EntryEvent<String, HazelcastJsonValue> event) {
        System.out.println("Entry Updated:" + event);
    }

}
