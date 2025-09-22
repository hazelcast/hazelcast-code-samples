package com.hazelcast.samples.testing;

import java.io.Serializable;

public record Customer(String id, String name)
        implements Serializable {
}
