package com.hazelcast.springHibernate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 17.07.2014.
 */

public interface ICustomerDAO {
 

    void addCustomer(Customer customer);
    

	void addCustomers(Map<String, Customer> customerMap);
	

	void deleteCustomer(String id);
    

    List<Customer> getCustomers();
 

    Customer getCustomerById(String id);
    

    Map<String, Customer> getCustomerMap(Collection<String> idCol);
    

    Set<String> getCustomerIds();
}
