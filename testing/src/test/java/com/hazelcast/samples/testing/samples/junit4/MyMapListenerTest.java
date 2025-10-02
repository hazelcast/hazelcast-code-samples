package com.hazelcast.samples.testing.samples.junit4;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Verifies that a map listener is triggered when an entry is updated.
 */
@RunWith(JUnit4.class)
public class MyMapListenerTest extends HazelcastTestSupport {

    private HazelcastInstance instance;

    @Before
    public void setUp() {
        instance = createHazelcastInstance();
    }

    @After
    public void tearDown() {
        if (instance != null) {
            instance.shutdown();
        }
    }

    /**
     * Insert and then update a map entry, verifying the registered
     * listener receives the update event once.
     */
    @Test
    public void updateTriggersListener() {
        EntryUpdatedListener<String, String> mockListener = mock(EntryUpdatedListener.class);

        IMap<String, String> map = instance.getMap("test-map");
        map.addEntryListener(mockListener, true);

        map.put("key1", "initial");
        map.put("key1", "updated");

        verify(mockListener, timeout(1000).times(1)).entryUpdated(any(EntryEvent.class));
    }
}
