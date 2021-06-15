package com.hazelcast.samples.serialization.benchmarks;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Multiple data items in one of the serialization formats.
 * </p>
 * <p>Use "{@code Object}" as type, too difficult to extend
 * a common type with code generators.
 * </p>
 *
 */
public class PersonCollection {

    private final ArrayList<Object> data;
    private final MyConstants.Kind kind;

    public PersonCollection(ArrayList<Object> arg0, MyConstants.Kind arg1) {
        this.data = arg0;
        this.kind = arg1;
    }

    public MyConstants.Kind getKind() {
        return this.kind;
    }

    public List<Object> getData() {
        return this.data;
    }
}
