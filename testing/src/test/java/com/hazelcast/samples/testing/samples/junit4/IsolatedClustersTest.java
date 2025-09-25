package com.hazelcast.samples.testing.samples.junit4;

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
 * Demonstrates running multiple Hazelcast clusters in parallel
 * to test business logic in isolation.
 *
 * <p>Illustrates running isolated, parallel test clusters using randomName(), HazelcastParallelClassRunner, and client+member provisioning.
 */
@RunWith(HazelcastParallelClassRunner.class)
public class IsolatedClustersTest {

    private HazelcastInstance[] members;
    private HazelcastInstance client;
    private TestHazelcastFactory factory;

    @Before
    public void setUp() {
        String clusterName = HazelcastTestSupport.randomName();
        Config serverConfig = new Config().setClusterName(clusterName);
        ClientConfig clientConfig = new ClientConfig().setClusterName(clusterName);

        factory = new TestHazelcastFactory(2);
        members = factory.newInstances(serverConfig, 2);
        client = factory.newHazelcastClient(clientConfig);
    }

    @After
    public void tearDown() {
        if (factory != null) {
            factory.shutdownAll();
        }
    }

    /**
     * Verify that business logic increments a map value,
     * and that the isolated cluster processes state correctly.
     */
    @Test
    public void isolatedClustersDontInterfere_clusterA() {
        IMap<String, Integer> map = client.getMap("isolatedMap");
        map.put("key", 1);
        map.executeOnKey("key",
                (EntryProcessor<String, Integer, Integer>) entry -> entry.setValue(entry.getValue() + 1));

        assertClusterSizeEventually(2, members[0]);
        assertEqualsEventually(() -> map.get("key"), 2);
    }

    /**
     * Verify that business logic decrements a map value,
     * and that the isolated cluster processes state correctly.
     */
    @Test
    public void isolatedClustersDontInterfere_clusterB() {
        IMap<String, Integer> map = client.getMap("isolatedMap");
        map.put("key", 1);
        map.executeOnKey("key",
                (EntryProcessor<String, Integer, Integer>) entry -> entry.setValue(entry.getValue() - 1));

        assertClusterSizeEventually(2, members[0]);
        assertEqualsEventually(() -> map.get("key"), 0);
    }
}
