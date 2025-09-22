package com.hazelcast.samples.testing;

import java.io.Serializable;

public record EnrichedOrder(String orderId, String customerName, String product)
        implements Serializable {
}
