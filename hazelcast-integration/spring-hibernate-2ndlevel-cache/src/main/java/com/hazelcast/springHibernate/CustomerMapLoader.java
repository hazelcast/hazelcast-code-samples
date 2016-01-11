package com.hazelcast.springHibernate;

import com.hazelcast.core.MapLoader;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class CustomerMapLoader implements MapLoader<String, Customer> {

    @Override
    public Customer load(String id) {
        Customer customer = getCustomerDAO().getCustomerById(id);
        System.out.println("load method is being processed. Customer : " + customer);
        return customer;
    }

    @Override
    public Map<String, Customer> loadAll(Collection<String> idCol) {
        Map<String, Customer> customerMap = getCustomerDAO().getCustomerMap(idCol);
        System.out.println("loadAll method is being processed. CustomerMap : " + customerMap);
        return customerMap;
    }

    @Override
    public Set<String> loadAllKeys() {
        return null;
    }

    public CustomerDAO getCustomerDAO() {
        return ApplicationContextUtil.getApplicationContext().getBean(CustomerDAO.class);
    }
}
