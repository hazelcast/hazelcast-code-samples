package com.hazelcast.samples.testing.samples.junit5;

import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.hazelcast.test.HazelcastTestSupport.assertClusterSize;
import static com.hazelcast.test.HazelcastTestSupport.assertClusterSizeEventually;
import static com.hazelcast.test.HazelcastTestSupport.assertTrueEventually;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies cluster formation, membership visibility, and basic
 * distributed data consistency across members and client.
 */
class MyClusterTest {

    private static final TestHazelcastFactory factory = new TestHazelcastFactory(2);
    private static HazelcastInstance member1;
    private static HazelcastInstance member2;
    private static HazelcastInstance client;

    @BeforeAll
    static void setupCluster() {
        member1 = factory.newHazelcastInstance();
        member2 = factory.newHazelcastInstance();
        client = factory.newHazelcastClient();
    }

    @AfterAll
    static void tearDownCluster() {
        factory.shutdownAll();
    }

    /**
     * Wait until both members see a 2-node cluster.
     */
    @Test
    void testClusterSizeEventually() {
        assertClusterSizeEventually(2, member1);
        assertClusterSizeEventually(2, member2);
    }

    /**
     * Check cluster size immediately without waiting.
     */
    @Test
    void testClusterSize() {
        assertClusterSize(2, member1);
        assertClusterSize(2, member2);
    }

    /**
     * Verify that cluster membership is visible across members and to the client.
     */
    @Test
    void testClusterFormed() {
        assertEquals(2, member1.getCluster().getMembers().size());
        assertTrue(client.getCluster().getMembers()
                         .contains(member2.getCluster().getLocalMember()));
    }

    /**
     * Simulate an async workload and verify map state converges across members.
     */
    @Test
    void testAsyncTasks() throws Exception {
        Runnable task = () -> {
            IMap<Integer, String> map = member1.getMap("map");
            map.put(1, "one");
            HazelcastTestSupport.sleepMillis(50);
            map.put(2, "two");
            HazelcastTestSupport.sleepMillis(100);
        };

        Thread t = new Thread(task);
        t.start();
        t.join();

        assertTrueEventually(() -> assertEquals(2, member2.getMap("map").size()));
        assertTrueEventually(() -> assertFalse(member2.getMap("map").containsKey("3")));
    }
}
