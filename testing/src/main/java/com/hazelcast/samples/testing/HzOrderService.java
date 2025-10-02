package com.hazelcast.samples.testing;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryUpdatedListener;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * {@link OrderService} backed by Hazelcast maps.
 *
 * <p>Orders are stored in the distributed map {@code "orders"}.
 * Customer validation is performed against the {@code "customers"} map
 * before persisting a new order.
 *
 * <p>An optional callback can be registered to receive notifications
 * when an order is updated in the cluster.
 */
public class HzOrderService implements OrderService {

    private final HazelcastInstance instance;

    /**
     * Create a new service without an update callback.
     *
     * @param hz Hazelcast client or member instance
     */
    public HzOrderService(HazelcastInstance hz) {
        this(hz, null);
    }

    /**
     * Create a new service with an optional update callback.
     *
     * @param hz                   Hazelcast client or member instance
     * @param onEntryUpdatedCallback callback invoked when an order is updated; may be {@code null}
     */
    public HzOrderService(HazelcastInstance hz, Consumer<Order> onEntryUpdatedCallback) {
        this.instance = hz;
        if (onEntryUpdatedCallback != null) {
            orderMap().addEntryListener(new OrderUpdatedListener(onEntryUpdatedCallback), true);
        }
    }

    private IMap<String, Order> orderMap() {
        return instance.getMap("orders");
    }

    private IMap<String, Customer> customerMap() {
        return instance.getMap("customers");
    }

    /**
     * Place a new order, validating that the referenced customer exists.
     *
     * @param order order to place
     * @throws IllegalStateException if the customer does not exist
     */
    @Override
    public void placeOrder(Order order) {
        Customer customer = customerMap().get(order.customerId());
        if (customer == null) {
            throw new IllegalStateException("Customer does not exist: " + order.customerId());
        }
        updateOrder(order);
    }

    /**
     * Retrieve an order by its identifier.
     *
     * @param id order identifier
     * @return the matching order, or {@code null} if not present
     */
    @Override
    public Order getOrder(String id) {
        return orderMap().get(id);
    }

    /**
     * Store or update an order in the distributed map.
     *
     * @param order order to update
     */
    @Override
    public void updateOrder(Order order) {
        orderMap().put(order.id(), order);
    }

    /**
     * Hazelcast entry listener that delegates order update events
     * to a provided {@link Consumer}.
     */
    static class OrderUpdatedListener
            implements EntryUpdatedListener<String, Order>, Serializable {

        private final Consumer<Order> listener;

        OrderUpdatedListener(Consumer<Order> listener) {
            this.listener = listener;
        }

        @Override
        public void entryUpdated(EntryEvent<String, Order> event) {
            listener.accept(event.getValue());
        }
    }
}
