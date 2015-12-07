package com.test.customer;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;

import java.text.ParseException;
import java.util.Set;

public class AgeExtractorDemo {

    public static void main(String[] args) throws ParseException {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<Integer, Customer> map = hz.getMap("customers");

        map.put(1, new Customer("James", "Bond", 1975));
        map.put(2, new Customer("Eathen", "Hunt", 1965));
        map.put(3, new Customer("Roger", "Moore", 1995));

        // we're using a custom attribute 'age' which is provided by the 'AgeExtractor'
        Set<Customer> customers = (Set<Customer>) map.values(new SqlPredicate("age < 50"));
        System.out.println("Customers: " + customers);

        Hazelcast.shutdownAll();
    }
}
