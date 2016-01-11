package com.test.query;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.test.query.QueryCollectionsDemo.Limb.limb;

public class QueryCollectionsDemo {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<String, Person> map = hz.getMap("map");

        map.put("1", new Person("peter", limb("left-hand"), limb("right-hand")));
        map.put("2", new Person("hans", limb("left-leg"), limb("right-leg")));

        Set<Person> employees = (Set<Person>) map.values(new SqlPredicate("limbs[any].name == right-leg"));
        System.out.println("People: " + employees);

        Hazelcast.shutdownAll();
    }

    static final class Person implements Serializable {

        final String name;
        final List<Limb> limbs;

        private Person(String name, Limb... limbs) {
            this.name = name;
            this.limbs = Arrays.asList(limbs);
        }

        @Override
        public String toString() {
            return "Person{"
                    + "name='" + name + '\''
                    + ", limbs=" + limbs
                    + '}';
        }
    }

    static final class Limb implements Serializable {

        final String name;

        Limb(String name) {
            this.name = name;
        }

        static Limb limb(String name) {
            return new Limb(name);
        }

        @Override
        public String toString() {
            return "Limb{"
                    + "name='" + name + '\''
                    + '}';
        }
    }
}
