package com.hazelcast.examples;

import com.hazelcast.monitor.NearCacheStats;
import org.junit.After;
import org.junit.Test;

import static com.hazelcast.examples.NearCacheSupport.shutdown;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NearCacheSamplesTest {

    @After
    public void cleanup() {
        shutdown();
    }

    @Test
    public void testNearCacheWithEvictionPolicy() {
        ClientNearCacheWithEvictionPolicy test = new ClientNearCacheWithEvictionPolicy();
        assertStats(test.run(), 100, 101, 101, 1, 0);
    }

    @Test
    public void testNearCacheWithInvalidation() {
        ClientNearCacheWithInvalidation test = new ClientNearCacheWithInvalidation();
        assertStats(test.run(), 1, 0, 2, 0, 0);
    }

    @Test
    public void testNearCacheWithMaxIdle() {
        ClientNearCacheWithMaxIdle test = new ClientNearCacheWithMaxIdle();
        assertStats(test.run(), 1, 20, 1, 0, 1);
    }

    @Test
    public void testNearCacheWithMemoryFormatBinary() {
        ClientNearCacheWithMemoryFormatBinary test = new ClientNearCacheWithMemoryFormatBinary();
        assertFalse(test.run());
    }

    @Test
    public void testNearCacheWithMemoryFormatObject() {
        ClientNearCacheWithMemoryFormatObject test = new ClientNearCacheWithMemoryFormatObject();
        assertTrue(test.run());
    }

    @Test
    public void testNearCacheWithPreloader() {
        ClientNearCacheWithPreloader test = new ClientNearCacheWithPreloader();
        assertEquals(ClientNearCacheWithPreloader.MAP_SIZE, test.run());
    }

    @Test
    public void testNearCacheWithTTL() {
        ClientNearCacheWithTTL test = new ClientNearCacheWithTTL();
        assertStats(test.run(), 1, 1, 1, 0, 1);
    }

    private void assertStats(NearCacheStats actualStats, long expectedEntryCount, long expectedHits, long expectedMisses, long expectedEvictions, long expectedExpirations) {
        assertEquals(expectedEntryCount, actualStats.getOwnedEntryCount());
        assertEquals(expectedHits, actualStats.getHits());
        assertEquals(expectedMisses, actualStats.getMisses());
        assertEquals(expectedEvictions, actualStats.getEvictions());
        assertEquals(expectedExpirations, actualStats.getExpirations());
    }
}
