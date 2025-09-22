package com.hazelcast.samples.testing;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryUpdatedListener;

import java.io.Serializable;
import java.util.function.Consumer;

public class HzOrderService
        implements OrderService {

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

    private final HazelcastInstance instance;

    public HzOrderService(HazelcastInstance hz) {
        this(hz, null);
    }

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

    @Override
    public void placeOrder(Order order) {
        // Enrich or validate order using shared customer state
        Customer customer = customerMap().get(order.customerId());
        if (customer == null) {
            throw new IllegalStateException("Customer does not exist: " + order.customerId());
        }
        updateOrder(order);
    }

    @Override
    public Order getOrder(String id) {
        return orderMap().get(id);
    }

    @Override
    public void updateOrder(Order order) {
        orderMap().put(order.id(), order);
    }
}

