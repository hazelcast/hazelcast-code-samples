package com.hazelcast.samples.testing.samples.junit5;

import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.junit.jupiter.api.Test;

import static com.hazelcast.test.HazelcastTestSupport.assertClusterSizeEventually;
import static com.hazelcast.test.HazelcastTestSupport.assertTrueEventually;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Demonstrates client interaction with a multi-member Hazelcast cluster.
 */
public class MyClusterClientTest {

    /**
     * Verify that a client can write to the cluster and
     * observe entries written by other members.
     */
    @Test
    void testClientPutAndGetAcrossCluster() {
        TestHazelcastFactory factory = new TestHazelcastFactory(2);
        HazelcastInstance member1 = factory.newHazelcastInstance();
        HazelcastInstance member2 = factory.newHazelcastInstance();
        HazelcastInstance client = factory.newHazelcastClient();

        try {
            // given: a 2-node cluster with a connected client
            member2.getMap("map").put("key0", "value0");

            // when: client writes a new entry
            IMap<String, String> clientMap = client.getMap("map");
            clientMap.put("key1", "value1");

            // then: verify both cluster and client see the data
            assertClusterSizeEventually(2, member1);
            assertTrueEventually(() -> assertEquals("value0", clientMap.get("key0")));
            assertTrueEventually(() -> assertEquals("value1", clientMap.get("key1")));
        } finally {
            factory.shutdownAll();
        }
    }
}
