package com.test.car.attribute;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;
import com.test.car.Car;

import java.text.ParseException;
import java.util.Set;

public class CarAttributeDemo {

    public static void main(String[] args) throws ParseException {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<Integer, Car> map = hz.getMap("cars");

        map.put(1, new Car("Audi Q7", 250, 22000));
        map.put(2, new Car("BMW X5", 312, 34000));
        map.put(3, new Car("Porsche Cayenne", 408, 57000));

        // we're using a custom attribute called 'attribute' which is provided by the 'CarAttributeExtractor'
        // we are also passing an argument 'mileage' to the extractor
        Set<Car> cars = (Set<Car>) map.values(new SqlPredicate("attribute[mileage] < 30000"));
        System.out.println("Cars: " + cars);

        Hazelcast.shutdownAll();
    }
}
