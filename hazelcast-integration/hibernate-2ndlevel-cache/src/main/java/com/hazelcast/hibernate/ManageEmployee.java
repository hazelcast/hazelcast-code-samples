package com.hazelcast.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.List;
import java.util.Scanner;

public class ManageEmployee {

    @SuppressWarnings({"checkstyle:cyclomaticcomplexity", "checkstyle:methodlength"})
    public static void main(String[] args) throws InterruptedException {
        SessionFactory factory;
        try {
            factory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Failed to create sessionFactory object: " + ex.getMessage());
            throw new ExceptionInInitializerError(ex);
        }

        Scanner reader = new Scanner(System.in);
        Session session1 = factory.openSession();
        Transaction tx1 = session1.beginTransaction();
        Session session2 = factory.openSession();
        Transaction tx2 = session2.beginTransaction();
        Session currentSession = session1;
        Transaction currentTx = tx1;
        int current = 1;

        while (true) {
            Thread.sleep(100);
            System.out.print("[" + current + ". session] Enter command: ");
            String command = reader.nextLine();
            if (command.equals("list")) {
                List employees = currentSession.createQuery("FROM Employee").list();
                for (Object entry : employees) {
                    Employee employee = (Employee) entry;
                    System.out.print("Id: " + employee.getId());
                    System.out.print(", first name: " + employee.getFirstName());
                    System.out.print(", last name: " + employee.getLastName());
                    System.out.println(", salary: " + employee.getSalary());
                }
            } else if (command.equals("add")) {
                System.out.print("Id: ");
                int id = reader.nextInt();
                reader.nextLine();
                System.out.print("First name: ");
                String fname = reader.nextLine();
                System.out.print("Last name: ");
                String lname = reader.nextLine();
                System.out.print("Salary: ");
                int salary = reader.nextInt();
                reader.nextLine();
                Employee employee = new Employee(id, fname, lname, salary);
                currentSession.save(employee);
            } else if (command.equals("delete")) {
                System.out.print("EmployeeID: ");
                int employeeId = reader.nextInt();
                reader.nextLine();
                Employee employee;
                employee = (Employee) currentSession.get(Employee.class, employeeId);
                currentSession.delete(employee);
            } else if (command.equals("close")) {
                currentTx.commit();
                currentSession.close();
            } else if (command.equals("open")) {
                if (current == 1) {
                    session1 = factory.openSession();
                    tx1 = session1.beginTransaction();
                    currentSession = session1;
                    currentTx = tx1;
                } else {
                    session2 = factory.openSession();
                    tx2 = session2.beginTransaction();
                    currentSession = session2;
                    currentTx = tx2;
                }
            } else if (command.equals("help")) {
                System.out.println("help         this menu");
                System.out.println("list         list all employees");
                System.out.println("add          add an employee");
                System.out.println("delete       delete and employee");
                System.out.println("open         open session and begin transaction");
                System.out.println("close        commit transaction and close session");
                System.out.println("change       change between two sessions");
                System.out.println("exit         exit");
            } else if (command.equals("exit")) {
                if (!tx1.wasCommitted()) {
                    tx1.commit();
                    session1.close();
                }
                if (!tx2.wasCommitted()) {
                    tx2.commit();
                    session2.close();
                }
                factory.close();
                break;
            } else if (command.equals("change")) {
                if (currentSession.equals(session1)) {
                    currentSession = session2;
                    currentTx = tx2;
                    current = 2;
                } else {
                    currentSession = session1;
                    currentTx = tx1;
                    current = 1;
                }
            } else {
                System.out.println("Command not found. Use help.");
            }
        }
    }
}
