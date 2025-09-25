package com.hazelcast.samples.testing.junit4;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.samples.testing.Customer;
import com.hazelcast.samples.testing.HzCustomerService;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link HzCustomerService} using helpers from {@link HazelcastTestSupport}.
 *
 * <p>Shows single-node vs multi-node setups via createHazelcastInstance(s) and validates data visibility across members.
 */
@RunWith(JUnit4.class)
public class CustomerServiceWithSupportTest extends HazelcastTestSupport {
    private HazelcastInstance instance;
    private HazelcastInstance[] cluster;

    /**
     * Verify that a customer stored in a single node can be retrieved
     * through the service.
     */
    @Test
    public void findCustomerSingleNode() {
        instance = createHazelcastInstance();
        instance.getMap("customers").put("123", new Customer("123", "Alice"));
        HzCustomerService sut = new HzCustomerService(instance);
        assertEquals("Alice", sut.findCustomer("123").name());
    }

    /**
     * Verify that a customer stored on one node can be read from another,
     * confirming cluster-wide data visibility.
     */
    @Test
    public void findCustomerTwoNodes() {
        cluster = createHazelcastInstances(2);
        HazelcastInstance node1 = cluster[0];
        HazelcastInstance node2 = cluster[1];

        // data injected in node1
        node1.getMap("customers").put("123", new Customer("123", "Alice"));

        // data retrieved from node2
        HzCustomerService sut2 = new HzCustomerService(node2);

        assertEquals("Alice", sut2.findCustomer("123").name());
    }

    /**
     * Shut down any Hazelcast members started for the tests.
     */
    @After
    public void tearDown() {
        if (instance != null) {
            instance.shutdown();
        }
        if (cluster != null) {
            Arrays.stream(cluster).forEach(HazelcastInstance::shutdown);
        }
    }
}
