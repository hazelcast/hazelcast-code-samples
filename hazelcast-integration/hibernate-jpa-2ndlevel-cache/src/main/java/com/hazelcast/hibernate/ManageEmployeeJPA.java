package com.hazelcast.hibernate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Scanner;

/**
 * Created by tgrl on 29.01.2015.
 */
public class ManageEmployee {

    private static EntityManager em;
    private static Scanner reader;
    private static String command;

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hiberjpa");
        em = emf.createEntityManager();
        
        System.out.println("Command: ");
        
        reader = new Scanner(System.in);
        
        
        while (true){
            command = reader.nextLine();
            if (command.equals("list")){

                System.out.println("List: ");
                listEmployee();
                reader.nextLine();

            }else if (command.equals("add")){

                System.out.print("Id: ");
                int id = reader.nextInt();
                reader.nextLine();
                System.out.print("First Name: ");
                String fname = reader.nextLine();
                System.out.print("Last Name: ");
                String lname = reader.nextLine();
                System.out.print("Salary: ");
                int salary = reader.nextInt();
                reader.nextLine();
                
                createEmployee(id, fname, lname, salary);

            }else if (command.equals("remove")){

                System.out.println("Key: ");
                int id = reader.nextInt();
                removeEmployee(id);
                reader.nextLine();

            }else if (command.equals("update")){

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
                reader.nextLine();
                
                updateEmployee(id, fname, lname, salary, key);

            }
        }
        

    }

    private static void createEmployee(int id, String first_name, String last_name, int salary) {
        Employee emp = new Employee(id, first_name, last_name, salary);
        em.getTransaction().begin();
        em.persist(emp);
        em.getTransaction().commit();
    }
    
    private static void removeEmployee(int key){
        Employee employee = em.find(Employee.class, key);
        em.getTransaction().begin();
        em.remove(employee);
        em.getTransaction().commit();
        
    }
    
    private static void listEmployee(){
        List<Employee> employeeList = em.createQuery("Select a from Employee a", Employee.class).getResultList();

        for (Employee employee : employeeList){
            System.out.println("" + employee.getFirstName());
        }
        
    }
    
    private static void updateEmployee(int id, String first_name, String last_name, int salary, int key){
        Employee employee = em.find(Employee.class, key);
        em.getTransaction().begin();
        employee.setId(id);
        employee.setFirstName(first_name);
        employee.setLastName(last_name);
        employee.setSalary(salary);
        em.getTransaction().commit();
        
    }

}

