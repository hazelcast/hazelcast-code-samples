package com.hazelcast.springHibernate;

import com.hazelcast.core.MapLoader;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 17.07.2014.
 */

public class CustomerMapLoader implements MapLoader<String, Customer> {

	private static final Logger logger = Logger.getLogger(CustomerMapLoader.class);
		

	@Override
	public Customer load(String id) {
		Customer customer = getCustomerDAO().getCustomerById(id);
		logger.debug("load method is being processed. Customer : " + customer);
		return customer;
	}


	@Override
	public Map<String, Customer> loadAll(Collection<String> idCol) {
		Map<String, Customer> customerMap = getCustomerDAO().getCustomerMap(idCol);
		logger.debug("loadAll method is being processed. CustomerMap : " + customerMap);
		return customerMap;
	}


	@Override
	public Set<String> loadAllKeys() {
		return null;
	}


	public ICustomerDAO getCustomerDAO() {
		return ApplicationContextUtil.getApplicationContext().getBean(CustomerDAO.class);
	}	
	
}
