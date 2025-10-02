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

/**
 * Tests Hazelcast integration with a mocked {@link MapStore}.
 *
 * <p>Exercises mocked MapStore for load success/failure and verifies write-behind (writeDelaySeconds) with timed verification.
 */
class MyMapStoreTest {

    private static TestHazelcastFactory factory;
    private static HazelcastInstance hz;

    @BeforeAll
    static void setup() {
        factory = new TestHazelcastFactory();

        @SuppressWarnings("unchecked")
        MapStore<String, String> mockMapStore = mock(MapStore.class);
        when(mockMapStore.load("fail")).thenThrow(new RuntimeException("Simulated failure"));
        when(mockMapStore.load("key1")).thenReturn("value1");

        Config config = new Config();
        config.setClusterName("mock-mapstore-test");
        config.getMapConfig("testMap")
              .getMapStoreConfig()
              .setEnabled(true)
              .setImplementation(mockMapStore);

        hz = factory.newHazelcastInstance(config);
    }

    @AfterAll
    static void teardown() {
        factory.shutdownAll();
    }

    /**
     * Verify a successful load from the mock MapStore.
     */
    @Test
    void testSuccessfulLoadFromMock() {
        IMap<String, String> map = hz.getMap("testMap");
        String result = map.get("key1");
        assertEquals("value1", result);
    }

    /**
     * Verify that an exception thrown from MapStore.load is propagated.
     */
    @Test
    void testLoadFailureHandled() {
        IMap<String, String> map = hz.getMap("testMap");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> map.get("fail")); // triggers MapStore.load("fail")

        assertEquals("Simulated failure", ex.getMessage());
    }

    /**
     * Verify that asynchronous store is invoked after write-delay.
     */
    @Test
    void testAsyncStoreIsInvoked() {
        @SuppressWarnings("unchecked")
        MapStore<String, String> mockMapStore = mock(MapStore.class);

        Config config = new Config().setClusterName("store-test");
        config.getMapConfig("storeMap")
              .getMapStoreConfig()
              .setEnabled(true)
              .setWriteDelaySeconds(1) // async write after 1s
              .setImplementation(mockMapStore);

        HazelcastInstance storeHz = factory.newHazelcastInstance(config);
        IMap<String, String> storeMap = storeHz.getMap("storeMap");

        storeMap.put("k2", "v2");

        // Verify store() was invoked within expected delay
        verify(mockMapStore, timeout(1500)).store("k2", "v2");

        storeHz.shutdown();
    }
}
