package com.test.portable;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import java.io.IOException;

public class Limb implements Portable {

    public static final int CLASS_ID = 1001;

    String name;

    public Limb() {
    }

    public Limb(String name) {
        this.name = name;
    }

    public static Limb limb(String name) {
        return new Limb(name);
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
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        name = in.readUTF("name");
    }

}
