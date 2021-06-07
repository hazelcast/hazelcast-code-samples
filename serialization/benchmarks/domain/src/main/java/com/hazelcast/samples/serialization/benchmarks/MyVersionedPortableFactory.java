package com.hazelcast.samples.serialization.benchmarks;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

public class MyVersionedPortableFactory implements PortableFactory {

    @Override
    public Portable create(int classId) {
        if (classId == MyConstants.PASSPORT_VERSIONED_PORTABLE_ID) {
            return new PassportVersionedPortable();
        }
        if (classId == MyConstants.PERSON_VERSIONED_PORTABLE_ID) {
            return new PersonVersionedPortable();
        }
        return null;
    }

}
