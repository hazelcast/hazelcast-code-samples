package com.hazelcast.samples.testing.samples.junit5;

import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.hazelcast.test.HazelcastTestSupport.assertEqualsEventually;

class MyClusterNameTest {

    private static HazelcastInstance member1;
    private static HazelcastInstance member2;
    private static String clusterName;

    @BeforeAll
    static void setupCluster() {
        clusterName = HazelcastTestSupport.randomName();
        Config config = new Config().setClusterName(clusterName);
        TestHazelcastFactory factory = new TestHazelcastFactory(2);
        member1 = factory.newHazelcastInstance(config);
        member2 = factory.newHazelcastInstance(config);
    }

    @AfterAll
    static void tearDownCluster() {
        member2.shutdown();
        member1.shutdown();
    }

    @Test
    void testClusterName() {
        assertEqualsEventually(() -> member1.getConfig().getClusterName(), clusterName);
        assertEqualsEventually(() -> member2.getConfig().getClusterName(), clusterName);
    }
}

