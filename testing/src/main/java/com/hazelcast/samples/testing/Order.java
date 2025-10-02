package com.hazelcast.samples.testing;

import java.io.Serializable;

/**
 * Order domain object with basic lifecycle state.
 *
 * <p>Immutable and serializable so it can be stored in Hazelcast
 * data structures. An order starts unconfirmed and can be marked
 * confirmed once processed.
 *
 * @param id         unique order identifier
 * @param customerId identifier of the customer placing the order
 * @param product    product being ordered
 * @param confirmed  {@code true} if the order has been confirmed
 */
public record Order(String id, String customerId, String product, boolean confirmed)
        implements Serializable {

    /**
     * Create a new unconfirmed order.
     *
     * @param id         order identifier
     * @param customerId customer identifier
     * @param product    product name
     */
    public Order(String id, String customerId, String product) {
        this(id, customerId, product, false);
    }

    /**
     * Return a confirmed copy of this order.
     *
     * @return new {@code Order} instance with {@code confirmed = true}
     */
    public Order confirm() {
        return new Order(id, customerId, product, true);
    }
}
