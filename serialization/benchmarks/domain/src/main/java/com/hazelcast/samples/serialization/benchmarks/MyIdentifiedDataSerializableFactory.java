package com.hazelcast.samples.serialization.benchmarks;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class MyIdentifiedDataSerializableFactory implements DataSerializableFactory {

    @Override
    public IdentifiedDataSerializable create(int classId) {
        if (classId == MyConstants.PASSPORT_IDENTIFIED_DATA_SERIALIZABLE_ID) {
            return new PassportIdentifiedDataSerializable();
        }
        if (classId == MyConstants.PERSON_IDENTIFIED_DATA_SERIALIZABLE_ID) {
            return new PersonIdentifiedDataSerializable();
        }
        return null;
    }

}
