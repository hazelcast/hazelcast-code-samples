package com.hazelcast.samples.testing.junit4;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.samples.testing.Customer;
import com.hazelcast.samples.testing.CustomerService;
import com.hazelcast.samples.testing.HzCustomerService;
import com.hazelcast.samples.testing.HzOrderService;
import com.hazelcast.samples.testing.Order;
import com.hazelcast.samples.testing.OrderService;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

/**
 * Integration test to verify {@link CustomerService} and {@link OrderService}
 * work correctly when backed by the same Hazelcast cluster.
 *
 * <p>Shows how to stand up a member with HazelcastTestSupport and verify two services share cluster state end-to-end.
 */
@RunWith(JUnit4.class)
public class CustomerOrderServicesIntegrationTest extends HazelcastTestSupport {

    private HazelcastInstance instance;

    /**
     * Verify that customers and orders can be created, persisted,
     * and queried consistently across both services.
     */
    @Test
    public void customerAndOrderServicesIntegration() {
        // Create a shared Hazelcast instance
        instance = createHazelcastInstance();

        // Instantiate both services using same cluster
        CustomerService customerService = new HzCustomerService(instance);
        OrderService orderService = new HzOrderService(instance);

        // Add customer
        Customer alice = new Customer("c1", "Alice");
        customerService.save(alice);

        // Place an order for Alice
        Order order = new Order("o1", "c1", "Laptop");
        orderService.placeOrder(order);

        // Verify state across services
        assertEquals("Alice", customerService.findCustomer("c1").name());
        assertEquals("Laptop", orderService.getOrder("o1").product());
    }

    /**
     * Shut down the Hazelcast instance after each test run.
     */
    @After
    public void tearDown() {
        instance.shutdown();
    }
}
