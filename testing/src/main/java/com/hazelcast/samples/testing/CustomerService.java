package com.hazelcast.samples.testing;

public interface CustomerService {
    Customer findCustomer(String number);

    void save(Customer customer);
}
