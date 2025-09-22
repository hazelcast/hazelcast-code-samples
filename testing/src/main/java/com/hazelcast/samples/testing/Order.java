package com.hazelcast.samples.testing;

import java.io.Serializable;

public record Order(String id, String customerId, String product, boolean confirmed)
        implements Serializable {

    public Order(String id, String customerId, String product) {
        this(id, customerId, product, false);
    }

    public Order confirm() {
        return new Order(id, customerId, product, true);
    }
}
