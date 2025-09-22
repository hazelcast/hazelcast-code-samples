package com.hazelcast.samples.testing;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class HzCustomerService
        implements CustomerService {
    private final HazelcastInstance instance;

    public HzCustomerService(HazelcastInstance instance) {
        this.instance = instance;
    }

    @Override
    public Customer findCustomer(String id) {
        try {
            return customerMap().get(id);
        } catch (Exception e) {
            throw new ServiceException("Find customer failed", e);
        }
    }

    public void save(Customer customer) {
        try {
            customerMap().put(customer.id(), customer);
        } catch (Exception e) {
            throw new ServiceException("Save customer failed", e);
        }
    }

    private IMap<String, Customer> customerMap() {
        return instance.getMap("customers");
    }
}
