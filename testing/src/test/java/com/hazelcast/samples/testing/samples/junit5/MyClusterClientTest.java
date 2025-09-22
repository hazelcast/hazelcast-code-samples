package com.hazelcast.samples.testing.samples.junit5;

import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.junit.jupiter.api.Test;

import static com.hazelcast.test.HazelcastTestSupport.assertClusterSizeEventually;
import static com.hazelcast.test.HazelcastTestSupport.assertTrueEventually;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyClusterClientTest {

    @Test
    public void testClientPutAndGetAcrossCluster() {
        // given: a 2-node in-process cluster with client
        TestHazelcastFactory factory = new TestHazelcastFactory(2);
        HazelcastInstance member1 = factory.newHazelcastInstance();
        HazelcastInstance member2 = factory.newHazelcastInstance();

        HazelcastInstance client = factory.newHazelcastClient();

        member2.getMap("map").put("key0", "value0");
        // when: client puts an entry
        IMap<String, String> clientMap = client.getMap("map");
        clientMap.put("key1", "value1");

        // then: client and cluster see the entry
        assertClusterSizeEventually(2, member1);
        assertTrueEventually(() -> assertEquals("value0", clientMap.get("key0")));
        assertTrueEventually(() -> assertEquals("value1", clientMap.get("key1")));

        client.shutdown();
        member1.shutdown();
        member2.shutdown();
    }
}
