package com.test.portable;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

class PersonFactory implements PortableFactory {

    static final int FACTORY_ID = 1000;

    @Override
    public Portable create(int i) {
        if (i == Person.CLASS_ID) {
            return new Person();
        } else if (i == Limb.CLASS_ID) {
            return new Limb();
        }
        throw new IllegalArgumentException("Unsupported type " + i);
    }
}
