package com.hazelcast.samples.testing;

import java.io.Serializable;

/**
 * Customer domain object.
 *
 * <p>Immutable and serializable so it can be stored in Hazelcast
 * data structures. Both {@code id} and {@code name} are required.
 *
 * @param id   unique customer identifier
 * @param name customer name
 */
public record Customer(String id, String name)
        implements Serializable {
}
