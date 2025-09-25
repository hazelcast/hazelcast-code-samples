package com.hazelcast.samples.testing.samples.junit4;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class MyMapListenerTest extends HazelcastTestSupport {

    private HazelcastInstance instance;

    @BeforeEach
    public void setUp() {
        // create Hazelcast member
        instance = createHazelcastInstance();
    }

    @AfterEach
    public void tearDown() {
        instance.shutdown();
    }

    @Test
    public void updateTriggersListener() {
        // create mock listener
        EntryUpdatedListener<String, String> mockListener = mock(EntryUpdatedListener.class);

        // register the listener
        IMap<String, String> map = instance.getMap("test-map");
        map.addEntryListener(mockListener, true);

        // insert and update an entry
        map.put("key1", "initial");
        map.put("key1", "updated");

        // verify the listener received the update
        verify(mockListener, timeout(1000).times(1)).entryUpdated(any(EntryEvent.class));
    }
}
