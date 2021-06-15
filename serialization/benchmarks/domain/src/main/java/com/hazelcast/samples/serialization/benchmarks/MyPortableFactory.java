package com.hazelcast.samples.serialization.benchmarks;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

public class MyPortableFactory implements PortableFactory {

    @Override
    public Portable create(int classId) {
        if (classId == MyConstants.PASSPORT_PORTABLE_ID) {
            return new PassportPortable();
        }
        if (classId == MyConstants.PERSON_PORTABLE_ID) {
            return new PersonPortable();
        }
        return null;
    }

}
