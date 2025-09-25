package com.hazelcast.samples.testing;

import java.io.Serializable;

/**
 * Business view of an order including customer details.
 *
 * <p>Used to represent an order after enrichment, for example
 * joining order data with customer information inside Hazelcast.
 *
 * @param orderId       unique order identifier
 * @param customerName  name of the customer placing the order
 * @param product       product being ordered
 */
public record EnrichedOrder(String orderId, String customerName, String product)
        implements Serializable {
}
