package com.test.customer;

import java.io.Serializable;
import java.util.UUID;

public class Customer implements Serializable {

    private final String id = UUID.randomUUID().toString();

    public String name;
    public String surname;
    public int yearOfBirth;

    public Customer(String name, String surname, int yearOfBirth) {
        this.name = name;
        this.surname = surname;
        this.yearOfBirth = yearOfBirth;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", yearOfBirth=" + yearOfBirth +
                '}';
    }

}
