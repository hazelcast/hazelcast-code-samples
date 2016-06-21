package com.test.query;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.query.SqlPredicate;

import java.io.IOException;
import java.util.Set;

import static com.test.query.QueryCollectionsPortableDemo.LimbPortable.limb;

public class QueryCollectionsPortableDemo {

    public static void main(String[] args) {
        Config config = new Config();
        config.getSerializationConfig().addPortableFactory(PersonPortableFactory.FACTORY_ID, new PersonPortableFactory());
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

        IMap<Integer, PersonPortable> map = hz.getMap("map");

        map.put(1, new PersonPortable("Georg", limb("left-leg"), limb("right-leg")));
        map.put(2, new PersonPortable("Peter", limb("left-hand"), limb("right-hand")));
        map.put(3, new PersonPortable("Hans", limb("left-leg"), limb("right-leg")));
        map.put(4, new PersonPortable("Stefanie", limb("left-arm"), limb("right-arm")));

        Set<PersonPortable> employees = (Set<PersonPortable>) map.values(new SqlPredicate("limbs[any].name == right-leg"));
        System.out.println("People: " + employees);

        Hazelcast.shutdownAll();
    }

    private static final class PersonPortable implements Portable {

        static final int CLASS_ID = 1000;

        String name;
        Portable[] limbs;

        PersonPortable() {
        }

        PersonPortable(String name, LimbPortable... limbs) {
            this.name = name;
            this.limbs = limbs;
        }

        @Override
        public String toString() {
            return "Person{name='" + name + "'}";
        }

        @Override
        public int getFactoryId() {
            return PersonPortableFactory.FACTORY_ID;
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

    static class LimbPortable implements Portable {

        static final int CLASS_ID = 1001;

        String name;

        LimbPortable() {
        }

        LimbPortable(String name) {
            this.name = name;
        }

        static LimbPortable limb(String name) {
            return new LimbPortable(name);
        }

        @Override
        public int getFactoryId() {
            return PersonPortableFactory.FACTORY_ID;
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

    private static final class PersonPortableFactory implements PortableFactory {

        static final int FACTORY_ID = 1000;

        @Override
        public Portable create(int i) {
            if (i == PersonPortable.CLASS_ID) {
                return new PersonPortable();
            } else if (i == LimbPortable.CLASS_ID) {
                return new LimbPortable();
            }
            throw new IllegalArgumentException("Unsupported type " + i);
        }
    }
}
