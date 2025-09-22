package com.hazelcast.samples.testing.samples.junit5;

import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.MapStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MyMapStoreTest {

    private static TestHazelcastFactory factory;
    private static HazelcastInstance hz;

    @BeforeAll
    static void setup() {
        factory = new TestHazelcastFactory();

        // Create a mock MapStore that throws on load
        @SuppressWarnings("unchecked") MapStore<String, String> mockMapStore = mock(MapStore.class);
        when(mockMapStore.load("fail")).thenThrow(new RuntimeException("Simulated failure"));
        when(mockMapStore.load("key1")).thenReturn("value1");

        // Configure Hazelcast to use the mock MapStore
        Config config = new Config();
        config.setClusterName("mock-mapstore-test");
        config.getMapConfig("testMap").getMapStoreConfig().setEnabled(true).setImplementation(mockMapStore);

        hz = factory.newHazelcastInstance(config);
    }

    @AfterAll
    static void teardown() {
        if (hz != null) {
            hz.shutdown();
        }
        factory.shutdownAll();
    }

    @Test
    void testSuccessfulLoadFromMock() {
        IMap<String, String> map = hz.getMap("testMap");

        // This triggers MapStore.load("key1")
        String result = map.get("key1");
        assertEquals("value1", result);
    }

    @Test
    void testLoadFailureHandled() {
        IMap<String, String> map = hz.getMap("testMap");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            map.get("fail"); // triggers MapStore.load("fail")
        });

        assertEquals("Simulated failure", ex.getMessage());
    }

    @Test
    void testAsyncStoreIsInvoked() {
        MapStore<String, String> mockMapStore = mock(MapStore.class);

        // Configure and start another instance for the store test
        Config config = new Config().setClusterName("store-test");
        config.getMapConfig("storeMap").getMapStoreConfig().setEnabled(true).setWriteDelaySeconds(1) // async write to MapStore after 1s
              .setImplementation(mockMapStore);

        HazelcastInstance storeHz = factory.newHazelcastInstance(config);
        IMap<String, String> storeMap = storeHz.getMap("storeMap");

        storeMap.put("k2", "v2");

        // Verify that store was called with a timeout larger than the setWriteDelaySeconds
        verify(mockMapStore, timeout(1500)).store("k2", "v2");

        storeHz.shutdown();
    }
}
