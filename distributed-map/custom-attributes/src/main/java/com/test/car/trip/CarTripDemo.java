package com.test.car.trip;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;
import com.test.car.Car;

import java.text.ParseException;
import java.util.Set;

public class CarTripDemo {

    public static void main(String[] args) throws ParseException {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<Integer, Car> map = hz.getMap("cars");

        map.put(1, new Car("Audi Q7").setTrip(1000, 3000));
        map.put(2, new Car("BMW X5").setTrip(3000, 5000));
        map.put(3, new Car("Porsche Cayenne").setTrip(2000, 8000));

        // we're using a custom 'tripDistance' attribute which is provided by the 'CarTripDistanceExtractor'
        Set<Car> cars = (Set<Car>) map.values(new SqlPredicate("tripDistance < 3000"));
        System.out.println("Cars: " + cars);

        Hazelcast.shutdownAll();
    }
}
