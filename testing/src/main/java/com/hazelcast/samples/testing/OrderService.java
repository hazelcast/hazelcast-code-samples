package com.hazelcast.samples.testing;

public interface OrderService {
    void placeOrder(Order order);

    Order getOrder(String id);

    void updateOrder(Order order);
}
