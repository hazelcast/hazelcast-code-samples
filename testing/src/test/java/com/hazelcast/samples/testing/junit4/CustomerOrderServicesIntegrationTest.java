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

@RunWith(JUnit4.class)
public class CustomerOrderServicesIntegrationTest
        extends HazelcastTestSupport {

    private HazelcastInstance instance;

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

    @After
    public void tearDown() {
        instance.shutdown();
    }
}
