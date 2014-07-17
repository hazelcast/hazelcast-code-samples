package com.hazelcast.springHibernate;

import com.hazelcast.core.IMap;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 17.07.2014.
 */

public interface IDistributedMapService {


	void addToDistributedMap(Customer customer);


	void removeFromDistributedMap(Customer customer);


	IMap<String, Customer> getCustomerMap();
}
