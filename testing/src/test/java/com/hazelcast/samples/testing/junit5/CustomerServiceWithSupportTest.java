package com.hazelcast.samples.testing.junit5;

import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.samples.testing.Customer;
import com.hazelcast.samples.testing.HzCustomerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.hazelcast.test.HazelcastTestSupport.randomName;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link HzCustomerService} using multiple Hazelcast members.
 *
 * <p>Single vs two-node scenarios with newInstances(...) to test cluster access patterns and state propagation.
 */
public class CustomerServiceWithSupportTest {
    private TestHazelcastFactory factory;

    @BeforeEach
    void setup() {
        factory = new TestHazelcastFactory();
    }

    @AfterEach
    void tearDown() {
        if (factory != null) {
            factory.shutdownAll();
        }
    }

    /**
     * Verify that a customer stored in a single node can be retrieved
     * via the service.
     */
    @Test
    void findCustomerSingleNode() {
        HazelcastInstance instance = factory.newHazelcastInstance();
        instance.getMap("customers").put("123", new Customer("123", "Alice"));
        HzCustomerService sut = new HzCustomerService(instance);
        assertEquals("Alice", sut.findCustomer("123").name());
    }

    /**
     * Verify that data written on one node is visible on another,
     * confirming cluster-wide state propagation.
     */
    @Test
    void findCustomerTwoNodes() {
        HazelcastInstance[] cluster = factory.newInstances(new Config().setClusterName(randomName()), 2);
        HazelcastInstance node1 = cluster[0];
        HazelcastInstance node2 = cluster[1];

        // data injected in node1
        node1.getMap("customers").put("123", new Customer("123", "Alice"));

        // data retrieved from node2
        HzCustomerService sut2 = new HzCustomerService(node2);
        assertEquals("Alice", sut2.findCustomer("123").name());
    }
}
