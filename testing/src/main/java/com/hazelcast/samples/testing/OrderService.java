package com.hazelcast.samples.testing;

/**
 * Service interface for managing {@link Order} records.
 *
 * <p>Defines basic operations to place, retrieve, and update orders.
 * Implementations may validate orders against customer state or
 * persist them in different backends.
 */
public interface OrderService {

    /**
     * Place a new order.
     *
     * @param order order to create
     */
    void placeOrder(Order order);

    /**
     * Retrieve an order by its identifier.
     *
     * @param id order identifier
     * @return the matching order, or {@code null} if not found
     */
    Order getOrder(String id);

    /**
     * Update an existing order.
     *
     * @param order order to update
     */
    void updateOrder(Order order);
}
