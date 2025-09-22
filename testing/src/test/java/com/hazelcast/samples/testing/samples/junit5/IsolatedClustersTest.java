package com.hazelcast.samples.testing.samples.junit5;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.IMap;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.hazelcast.test.HazelcastTestSupport.assertClusterSizeEventually;
import static com.hazelcast.test.HazelcastTestSupport.assertEqualsEventually;

/**
 * This test shows how I can run multiple tests in parallel and in isolation, to test
 * business logic on Hazelcast data structures.
 * By running tests in parallel I can expedite their execution.
 * Isolation can be achieved by by assigning random names to the
 * cluster so each test doesn't interfere with the other.
 */
@RunWith(HazelcastParallelClassRunner.class)
public class IsolatedClustersTest {

    private HazelcastInstance[] members;
    private HazelcastInstance client;

    @Before
    void setUp() {
        String clusterName = HazelcastTestSupport.randomName();
        Config serverConfig = new Config().setClusterName(clusterName);
        ClientConfig clientConfig = new ClientConfig().setClusterName(clusterName);

        TestHazelcastFactory factory = new TestHazelcastFactory(2);
        members = factory.newInstances(serverConfig, 2);
        client = factory.newHazelcastClient(clientConfig);
    }

    @After
    void tearDown()
            throws InterruptedException {
        for (HazelcastInstance instance : members) {
            instance.shutdown();
        }
        client.shutdown();
    }

    @Test
    void isolatedClustersDontInterfere_clusterA() {

        // Custom business logic
        IMap<String, Integer> map = client.getMap("isolatedMap");
        map.put("key", 1);
        map.executeOnKey("key", (EntryProcessor<String, Integer, Integer>) entry -> entry.setValue(entry.getValue() + 1));

        // verify cluster formed and data is available
        assertClusterSizeEventually(2, members[0]);
        assertEqualsEventually(() -> map.get("key"), 2);
    }

    @Test
    void isolatedClustersDontInterfere_clusterB() {
        // Custom business logic
        IMap<String, Integer> map = client.getMap("isolatedMap");
        map.put("key", 1);
        map.executeOnKey("key", (EntryProcessor<String, Integer, Integer>) entry -> entry.setValue(entry.getValue() - 1));

        // verify cluster formed and data is processed
        assertClusterSizeEventually(2, members[0]);
        assertEqualsEventually(() -> map.get("key"), 0);
    }
}
