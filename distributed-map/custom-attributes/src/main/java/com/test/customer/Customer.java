package com.test.customer;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings("unused")
public class Customer implements Serializable {

    private final String id = UUID.randomUUID().toString();

    private String name;
    private String surname;
    private int yearOfBirth;

    public Customer(String name, String surname, int yearOfBirth) {
        this.name = name;
        this.surname = surname;
        this.yearOfBirth = yearOfBirth;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    @Override
    public String toString() {
        return "Customer{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", surname='" + surname + '\''
                + ", yearOfBirth=" + yearOfBirth
                + '}';
    }
}
