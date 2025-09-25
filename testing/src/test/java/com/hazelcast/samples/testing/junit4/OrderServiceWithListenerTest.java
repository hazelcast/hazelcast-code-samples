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

/**
 * Verifies that {@link HzOrderService} correctly registers and triggers
 * an update listener on order changes.
 *
 * <p>Shows how to register an entry listener and verify cluster events with Mockito using a real member.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderServiceWithListenerTest extends HazelcastTestSupport {

    @Mock
    private Consumer<Order> mockConsumer;

    private HazelcastInstance instance;

    /**
     * Place and update an order, and verify that the listener is
     * triggered exactly once within the expected time window.
     */
    @Test
    public void testOrderServiceListener() {
        instance = createHazelcastInstance();

        // Add a customer so orders can be validated
        instance.getMap("customers").put("c1", new Customer("c1", "Alice"));

        OrderService sut = new HzOrderService(instance, mockConsumer);

        Order order = new Order("o1", "c1", "Laptop");
        sut.placeOrder(order);

        // Trigger listener by updating the order
        sut.updateOrder(order.confirm());

        // Verify callback fired exactly once within 100ms
        verify(mockConsumer, timeout(100).only()).accept(any(Order.class));
    }

    /**
     * Shut down the Hazelcast instance after test completion.
     */
    @After
    public void tearDown() {
        if (instance != null) {
            instance.shutdown();
        }
    }
}
