package com.hazelcast.samples.testing;

/**
 * Service interface for working with {@link Customer} objects.
 *
 * <p>Intended to demonstrate persisting and retrieving state
 * through Hazelcast.
 */
public interface CustomerService {

    /**
     * Look up a customer by identifier.
     *
     * @param number customer identifier
     * @return the matching customer, or {@code null} if not found
     */
    Customer findCustomer(String number);

    /**
     * Persist a customer.
     *
     * @param customer customer instance to store
     */
    void save(Customer customer);
}
