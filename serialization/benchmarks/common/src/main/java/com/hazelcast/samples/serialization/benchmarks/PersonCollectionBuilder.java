package com.hazelcast.samples.serialization.benchmarks;

/**
 * <p>A builder to build a collection of data for one
 * of the serialization types.
 * </p>
 */
public interface PersonCollectionBuilder {

    PersonCollectionBuilder addData(Object[][] raw);

    PersonCollection build();

}
