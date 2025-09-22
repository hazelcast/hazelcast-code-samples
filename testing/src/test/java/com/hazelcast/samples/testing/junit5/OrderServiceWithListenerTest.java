package com.hazelcast.samples.testing.junit5;

import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.samples.testing.Customer;
import com.hazelcast.samples.testing.HzOrderService;
import com.hazelcast.samples.testing.Order;
import com.hazelcast.samples.testing.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class OrderServiceWithListenerTest {

    private TestHazelcastFactory factory;

    @BeforeEach
    public void setup() {
        factory = new TestHazelcastFactory();
    }

    @AfterEach
    public void tearDown() {
        if (factory != null) {
            factory.shutdownAll();
        }
    }

    @Test
    public void orderServiceListenerIsInvoked() {
        HazelcastInstance instance = factory.newHazelcastInstance();
        // set a customer
        instance.getMap("customers").put("c1", new Customer("c1", "Alice"));

        Consumer<Order> mockConsumer = mock(Consumer.class);

        OrderService sut = new HzOrderService(instance, mockConsumer);

        Order order = new Order("o1", "c1", "Laptop");
        sut.placeOrder(order);
        // Update the order so hazelcast triggers the event
        sut.updateOrder(order.confirm());

        verify(mockConsumer, timeout(100).only()).accept(any(Order.class));
    }

}