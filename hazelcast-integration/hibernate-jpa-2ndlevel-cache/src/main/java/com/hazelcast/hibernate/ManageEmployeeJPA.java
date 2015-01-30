package com.hazelcast.hibernate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Created by tgrl on 29.01.2015.
 */
public class ManageEmployeeJPA {

    private static EntityManager em;

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hiberjpa");
        em = emf.createEntityManager();

        createEmployee(1, "Saint", "Peter", 100);
        createEmployee(2, "Jack", " Dorsey", 200);
        createEmployee(3, "Sam", "Fox", 300);

        Employee employee = em.find(Employee.class, 1);
        System.out.println("" + employee.getFirstName());

        em.getTransaction().begin();
        employee.setFirstName("Aaaaa");
        em.getTransaction().commit();
        System.out.println("" + employee.getFirstName());

        Employee employee1 = em.find(Employee.class, 2);
        em.getTransaction().begin();
        em.remove(employee1);
        em.getTransaction().commit();

    }

    private static void createEmployee(int id, String first_name, String last_name, int salary) {
        Employee emp = new Employee(id, first_name, last_name, salary);
        em.getTransaction().begin();
        em.persist(emp);
        em.getTransaction().commit();
    }

}

