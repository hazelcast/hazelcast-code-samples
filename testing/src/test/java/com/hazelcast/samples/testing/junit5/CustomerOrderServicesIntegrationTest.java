package com.hazelcast.samples.testing.junit5;

import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.samples.testing.Customer;
import com.hazelcast.samples.testing.CustomerService;
import com.hazelcast.samples.testing.HzCustomerService;
import com.hazelcast.samples.testing.HzOrderService;
import com.hazelcast.samples.testing.Order;
import com.hazelcast.samples.testing.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration test to verify {@link CustomerService} and {@link OrderService}
 * interact correctly when backed by the same Hazelcast cluster.
 *
 * <p>Uses TestHazelcastFactory in JUnit 5 to validate cross-service state on a shared member.
 */
public class CustomerOrderServicesIntegrationTest {

    private TestHazelcastFactory factory;

    @BeforeEach
    void setUp() {
        factory = new TestHazelcastFactory();
    }

    @AfterEach
    void tearDown() {
        if (factory != null) {
            factory.shutdownAll();
        }
    }

    /**
     * Verify that a customer can be created and an order
     * placed against it, with consistent state across both services.
     */
    @Test
    void customerAndOrderServicesIntegration() {
        HazelcastInstance instance = factory.newHazelcastInstance();

        CustomerService customerService = new HzCustomerService(instance);
        OrderService orderService = new HzOrderService(instance);

        Customer alice = new Customer("c1", "Alice");
        customerService.save(alice);

        Order order = new Order("o1", "c1", "Laptop");
        orderService.placeOrder(order);

        assertEquals("Alice", customerService.findCustomer("c1").name());
        assertEquals("Laptop", orderService.getOrder("o1").product());
    }
}
