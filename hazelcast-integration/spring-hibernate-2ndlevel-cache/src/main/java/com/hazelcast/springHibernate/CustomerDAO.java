package com.hazelcast.springHibernate;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 17.07.2014.
 */

@Transactional(readOnly = false)
public class CustomerDAO {
 
    private SessionFactory sessionFactory;

	@Transactional(readOnly = false)
    public void addCustomer(Customer customer) {
		sessionFactory.getCurrentSession().save(customer);    	
    }
	

	@Transactional(readOnly = false)
    public void addCustomers(Map<String, Customer> customerMap) {
		Collection<Customer> customerCol = customerMap.values();
		for(Customer customer : customerCol) {
			sessionFactory.getCurrentSession().save(customer); 
		}           	
    }

	@Transactional(readOnly = false)
    public void deleteCustomer(String id) {
        Query query = sessionFactory.getCurrentSession().createQuery("delete Customer where id=:id");
        query.setParameter("id", id);
        query.executeUpdate();   	
    }

    public List<Customer> getCustomers() {
        @SuppressWarnings("unchecked")
		List<Customer> list = sessionFactory.getCurrentSession().createQuery("from Customer").list();
        return list;
    }

	public Customer getCustomerById(String id) {
		Customer customer = (Customer) sessionFactory.getCurrentSession()
							                .createQuery("from Customer where id=?")
							                .setParameter(0, id).uniqueResult();
		return customer;
	}

	public Map<String, Customer> getCustomerMap(Collection<String> idCol) {
    	Map<String, Customer> customerMap = new HashMap();
		for(String id : idCol) {
			Customer customer = (Customer) sessionFactory
					                    .getCurrentSession()
						                .createQuery("from Customer where id=?")
						                .setParameter(0, id)
						                .uniqueResult();
			customerMap.put(customer.getId(), customer);
		}
		
		return customerMap;
	}

	public Set<String> getCustomerIds() {
		@SuppressWarnings("unchecked")
		List<String> customerIdList = sessionFactory
									.getCurrentSession()
							        .createQuery("select cus.id from Customer cus")
							        .list();
		return new HashSet<String>(customerIdList);
	}

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}
