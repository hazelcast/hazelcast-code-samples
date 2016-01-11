package com.hazelcast.springHibernate;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Transactional(readOnly = false)
@SuppressWarnings("unused")
public class CustomerDAO {

    private SessionFactory sessionFactory;

    @Transactional(readOnly = false)
    public void addCustomer(Customer customer) {
        sessionFactory.getCurrentSession().save(customer);
    }

    @Transactional(readOnly = false)
    public void addCustomers(Map<String, Customer> customerMap) {
        Collection<Customer> customerCol = customerMap.values();
        for (Customer customer : customerCol) {
            sessionFactory.getCurrentSession().save(customer);
        }
    }

    @Transactional(readOnly = false)
    public void deleteCustomer(String id) {
        Query query = sessionFactory.getCurrentSession().createQuery("delete Customer where id=:id");
        query.setParameter("id", id);
        query.executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public List<Customer> getCustomers() {
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
        Map<String, Customer> customerMap = new HashMap<String, Customer>();
        for (String id : idCol) {
            Customer customer = (Customer) sessionFactory
                    .getCurrentSession()
                    .createQuery("from Customer where id=?")
                    .setParameter(0, id)
                    .uniqueResult();
            customerMap.put(customer.getId(), customer);
        }
        return customerMap;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getCustomerIds() {
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
