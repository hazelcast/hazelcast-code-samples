package com.test.car;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Car implements Serializable {

    private Map<String, Object> attributes;

    public Car(String name) {
        attributes = new HashMap<String, Object>();
        attributes.put("name", name);
        attributes.put("tripStart", 0);
        attributes.put("tripStop", 0);
    }

    public Car(String name, Integer breakHorsePower, Integer mileage) {
        this(name);
        attributes.put("bhp", breakHorsePower);
        attributes.put("mileage", mileage);
    }

    public Car setTrip(Integer tripStartMileage, Integer tripEndMileage) {
        attributes.put("tripStart", tripStartMileage);
        attributes.put("tripStop", tripEndMileage);
        return this;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public String toString() {
        return "Car{"
                + "attributes=" + attributes
                + '}';
    }
}
