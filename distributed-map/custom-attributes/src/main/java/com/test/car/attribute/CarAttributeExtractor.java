package com.test.car.attribute;

import com.hazelcast.query.extractor.ValueCollector;
import com.hazelcast.query.extractor.ValueExtractor;
import com.test.car.Car;

public class CarAttributeExtractor extends ValueExtractor<Car, String> {

    @Override
    public void extract(Car car, String argument, ValueCollector valueCollector) {
        valueCollector.addObject(car.getAttribute(argument));
    }
}
