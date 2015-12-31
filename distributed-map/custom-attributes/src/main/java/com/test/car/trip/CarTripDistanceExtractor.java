package com.test.car.trip;

import com.hazelcast.query.extractor.ValueCollector;
import com.hazelcast.query.extractor.ValueExtractor;
import com.test.car.Car;

public class CarTripDistanceExtractor extends ValueExtractor<Car, String> {

    @Override
    public void extract(Car car, String argument, ValueCollector valueCollector) {
        Integer tripStartMileage = (Integer) car.getAttribute("tripStart");
        Integer tripStopMileage = (Integer) car.getAttribute("tripStop");
        valueCollector.addObject(tripStopMileage - tripStartMileage);
    }
}
