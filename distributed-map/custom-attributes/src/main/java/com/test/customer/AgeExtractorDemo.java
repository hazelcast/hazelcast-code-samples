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
        IMap<String, Customer> map = hz.getMap("people");

        map.put("1", new Customer("james", "bond", 1975));
        map.put("2", new Customer("eathen", "hunt", 1965));
        map.put("3", new Customer("roger", "moore", 1995));

        // We're using a custom attribute 'age' which is provided by the 'AgeExtractor'
        Set<Customer> employees = (Set<Customer>) map.values(new SqlPredicate("age < 50"));
        System.out.println("Employees:" + employees);
    }

}
