package com.hazelcast.samples.testing.junit4;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.samples.testing.Customer;
import com.hazelcast.samples.testing.HzOrderService;
import com.hazelcast.samples.testing.Order;
import com.hazelcast.samples.testing.OrderService;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceWithListenerTest
        extends HazelcastTestSupport {
    @Mock
    private Consumer<Order> mockConsumer;
    private HazelcastInstance instance;

    @Test
    public void testOrderServiceListener() {
        instance = createHazelcastInstance();
        // set a customer
        instance.getMap("customers").put("c1", new Customer("c1", "Alice"));

        OrderService sut = new HzOrderService(instance, mockConsumer);

        Order order = new Order("o1", "c1", "Laptop");
        sut.placeOrder(order);
        // Update the order so hazelcast triggers the event
        sut.updateOrder(order.confirm());

        // verifying that only mockConsumer#accept(Order) is called within a lag of 100ms
        verify(mockConsumer, timeout(100).only()).accept(any(Order.class));
    }

    @After
    public void tearDown() {
        if (instance != null) {
            instance.shutdown();
        }
    }
}
