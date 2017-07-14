package com.hazelcast.hibernate;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.hibernate.instance.HazelcastAccessor;
import org.hibernate.SessionFactory;
import org.hibernate.ejb.EntityManagerFactoryImpl;
import org.hibernate.jmx.StatisticsService;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author tgrl
 */
public class ManageEmployeeJPA {

    public static final String FIRST_NAME = "John ";
    public static final String LAST_NAME = "Dow ";
    public static final int ENTRY_COUNT = 20;
    public static final String SELECT_A_FROM_EMPLOYEE_A = "Select a from Employee a";
    private static EntityManager em;
    private static Statistics statistics;
    private static HazelcastInstance hazelcast;

    public static void main(String[] args) {
        init();
        populateDb();
        processConsoleCommand();
        Hazelcast.shutdownAll();
    }

    private static void populateDb() {
        for (int i = 1; i < ENTRY_COUNT; i++) {
            removeEmployee(i);
            createEmployee(i, FIRST_NAME + i, LAST_NAME + i, i);
        }
    }

    private static void init() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hiberjpa");
        em = emf.createEntityManager();
        EntityManagerFactoryImpl emfi = (EntityManagerFactoryImpl) emf;
        SessionFactory sessionFactory = emfi.getSessionFactory();
        statistics = sessionFactory.getStatistics();
        registerMBean(sessionFactory);
        hazelcast = HazelcastAccessor.getHazelcastInstance(sessionFactory);
    }

    private static void registerMBean(SessionFactory sessionFactory) {
        ArrayList<MBeanServer> list = MBeanServerFactory.findMBeanServer(null);
        MBeanServer server = list.get(0);
        try {
            ObjectName objectName = new ObjectName("org.hibernate:name=HibernateStatistics");
            StatisticsService mBean = new StatisticsService();
            mBean.setSessionFactory(sessionFactory);
            server.registerMBean(mBean, objectName);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            e.printStackTrace();
        }
    }

    private static void processConsoleCommand() {
        Scanner reader = new Scanner(System.in);
        while (true) {
            System.out.println("Command: ");
            String command = reader.nextLine();
            if (command.equals("list")) {
                System.out.println("List: ");
                listEmployee();
            } else if (command.equals("add")) {
                System.out.print("Id: ");
                int id = reader.nextInt();
                reader.nextLine();
                System.out.print("First Name: ");
                String fname = reader.nextLine();
                System.out.print("Last Name: ");
                String lname = reader.nextLine();
                System.out.print("Salary: ");
                int salary = reader.nextInt();
                createEmployee(id, fname, lname, salary);
            } else if (command.equals("remove")) {
                System.out.println("Key: ");
                int id = reader.nextInt();
                removeEmployee(id);
            } else if (command.equals("update")) {
                System.out.print("Id: ");
                int id = reader.nextInt();
                reader.nextLine();
                System.out.print("First Name: ");
                String fname = reader.nextLine();
                System.out.print("Last Name: ");
                String lname = reader.nextLine();
                System.out.print("Salary: ");
                int salary = reader.nextInt();
                System.out.print("Key: ");
                int key = reader.nextInt();
                updateEmployee(id, fname, lname, salary, key);
            } else if (command.equals("show")) {
                System.out.println("Key: ");
                int id = reader.nextInt();
                showEmployee(id);
            } else if (command.equals("stats")) {
                printStatistics();
            } else if (command.endsWith("exit")) {
                break;
            } else {
                System.err.println("Command not found: " + command);
            }
        }
    }

    private static void showEmployee(int id) {
        Employee employee = em.find(Employee.class, id);
        if (employee != null) {
            printEmployee(employee);
        } else {
            System.out.println("not found");
        }
    }

    private static void printStatistics() {
        printHibernateStatistics();
        printHazelcastStatistics();
    }

    private static void printHazelcastStatistics() {
        IMap<Object, Object> map = hazelcast.getMap(Employee.class.getName());
        System.out.println("Hazelcast.Map cache size is " + map.size() + " entries");
    }

    private static void printHibernateStatistics() {
        String name = Employee.class.getName();

        EntityStatistics entityStatistics = statistics.getEntityStatistics(name);
        if (entityStatistics != null) {
            System.out.println("Hibernate.EntityStatistics for " + name);
            System.out.println(entityStatistics);
        } else {
            System.err.println("Hibernate.EntityStatistics null for " + name);
        }

        QueryStatistics queryStats = statistics.getQueryStatistics(SELECT_A_FROM_EMPLOYEE_A);
        if (queryStats != null) {
            System.out.println("Hibernate.QueryStatistics for " + SELECT_A_FROM_EMPLOYEE_A);
            System.out.println(queryStats);
        } else {
            System.err.println("Hibernate.QueryStatistics null for " + SELECT_A_FROM_EMPLOYEE_A);
        }

        String regionName = "com.hazelcast.hibernate.Employee";
        SecondLevelCacheStatistics cacheStats = statistics.getSecondLevelCacheStatistics(regionName);
        if (cacheStats != null) {
            System.out.println("Hibernate.SecondLevelCacheStatistics stats for " + regionName);
            System.out.println(cacheStats);
        } else {
            System.err.println("Hibernate.SecondLevelCacheStatistics null for " + regionName);
        }
    }

    private static void createEmployee(int id, String firstName, String lastName, int salary) {
        Employee emp = new Employee(id, firstName, lastName, salary);
        em.getTransaction().begin();
        em.persist(emp);
        em.getTransaction().commit();
    }

    private static void removeEmployee(int key) {
        Employee employee = em.find(Employee.class, key);
        if (employee != null) {
            em.getTransaction().begin();
            em.remove(employee);
            em.getTransaction().commit();
        }
    }

    private static void listEmployee() {
        List<Employee> employeeList = em
                .createQuery(SELECT_A_FROM_EMPLOYEE_A, Employee.class)
                .getResultList();

        for (Employee employee : employeeList) {
            printEmployee(employee);
        }
    }

    private static void printEmployee(Employee employee) {
        System.out.println("ID: " + employee.getId());
        System.out.println("First name: " + employee.getFirstName());
        System.out.println("Last name: " + employee.getLastName());
        System.out.println("Salary: " + employee.getSalary());
    }

    private static void updateEmployee(int id, String firstName, String lastName, int salary, int key) {
        Employee employee = em.find(Employee.class, key);
        em.getTransaction().begin();
        employee.setId(id);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setSalary(salary);
        em.getTransaction().commit();
    }
}
