package com.hazelcast.springHibernate;

import com.hazelcast.core.IMap;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 17.07.2014.
 */

public class DistributedMapService {
	
	private IMap<String, Customer> customerMap;

	public DistributedMapService(IMap<String, Customer> customerMap) {
		setCustomerMap(customerMap);
	}

	public void addToDistributedMap(Customer customer) {
		getCustomerMap().put(customer.getId(), customer);
	}

	public void removeFromDistributedMap(Customer customer) {
		getCustomerMap().remove(customer.getId());
	}
	
	public IMap<String, Customer> getCustomerMap() {
		return customerMap;
	}

	public void setCustomerMap(IMap<String, Customer> customerMap) {
		this.customerMap = customerMap;
	}
	
}