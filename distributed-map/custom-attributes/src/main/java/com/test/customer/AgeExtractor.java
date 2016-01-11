package com.test.customer;

import com.hazelcast.query.extractor.ValueCollector;
import com.hazelcast.query.extractor.ValueExtractor;

import java.util.Calendar;

public class AgeExtractor extends ValueExtractor<Customer, String> {

    @Override
    public void extract(Customer customer, String argument, ValueCollector valueCollector) {
        int age = Calendar.getInstance().get(Calendar.YEAR) - customer.getYearOfBirth();
        valueCollector.addObject(age);
    }
}
