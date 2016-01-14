package com.hazelcast.hibernate;

/**
 * @author Esref Ozturk <esrefozturk93@gmail.com>
 */
@SuppressWarnings("unused")
public class Employee {

    private int id;
    private String firstName;
    private String lastName;
    private int salary;

    public Employee() {
    }

    public Employee(int id, String fname, String lname, int salary) {
        this.id = id;
        this.firstName = fname;
        this.lastName = lname;
        this.salary = salary;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }
}
