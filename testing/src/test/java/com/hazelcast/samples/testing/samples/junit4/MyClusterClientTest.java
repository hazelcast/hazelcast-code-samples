package com.hazelcast.samples.testing.samples.junit4;

import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class MyClusterClientTest
        extends HazelcastTestSupport {

    @Test
    public void testClientPutAndGetAcrossCluster() {
        // given: a 2-node in-process cluster with client
        TestHazelcastFactory factory = new TestHazelcastFactory(2);
        HazelcastInstance member1 = factory.newHazelcastInstance();
        HazelcastInstance member2 = factory.newHazelcastInstance();

        HazelcastInstance client = factory.newHazelcastClient();

        try {
            member2.getMap("map").put("key0", "value0");
            // when: client puts an entry
            IMap<String, String> clientMap = client.getMap("map");
            clientMap.put("key1", "value1");

            // then: client and cluster see the entry
            assertClusterSizeEventually(2, member1);
            assertTrueEventually(() -> assertEquals("value0", clientMap.get("key0")));
            assertTrueEventually(() -> assertEquals("value1", clientMap.get("key1")));
        } finally {
            factory.shutdownAll();
        }
    }
}
