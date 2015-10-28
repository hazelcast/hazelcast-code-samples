package com.test.customer;

import com.hazelcast.query.extractor.ValueExtractor;

public class AgeInMonthsExtractor extends ValueExtractor<Customer, Integer> {

    @Override
    public Integer extract(Customer customer) {
        return customer.age * 12;
    }

}