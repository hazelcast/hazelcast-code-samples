package com.hazelcast.samples.testing;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

/**
 * {@link CustomerService} backed by a Hazelcast {@link IMap}.
 *
 * <p>Stores and retrieves {@link Customer} objects in the
 * distributed map {@code "customers"}.
 */
public class HzCustomerService implements CustomerService {
    private final HazelcastInstance instance;

    /**
     * Creates a new service bound to a Hazelcast instance.
     *
     * @param instance Hazelcast client or member used to access the map
     */
    public HzCustomerService(HazelcastInstance instance) {
        this.instance = instance;
    }

    /**
     * Retrieve a customer from the distributed map.
     *
     * @param id customer identifier
     * @return the matching customer, or {@code null} if not present
     * @throws ServiceException if the lookup fails
     */
    @Override
    public Customer findCustomer(String id) {
        try {
            return customerMap().get(id);
        } catch (Exception e) {
            throw new ServiceException("Find customer failed", e);
        }
    }

    /**
     * Store or update a customer in the distributed map.
     *
     * @param customer customer instance to save
     * @throws ServiceException if the save operation fails
     */
    @Override
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
