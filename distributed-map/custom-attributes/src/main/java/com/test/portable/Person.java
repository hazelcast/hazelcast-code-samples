package com.test.portable;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import java.io.IOException;

public class Person implements Portable {

    static final int CLASS_ID = 1000;

    private String name;
    private Portable[] limbs;

    Person() {
    }

    Person(String name, Limb... limbs) {
        this.name = name;
        this.limbs = limbs;
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "'}";
    }

    @Override
    public int getFactoryId() {
        return PersonFactory.FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("name", name);
        out.writePortableArray("limbs", limbs);
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        name = in.readUTF("name");
        limbs = in.readPortableArray("limbs");
    }
}
