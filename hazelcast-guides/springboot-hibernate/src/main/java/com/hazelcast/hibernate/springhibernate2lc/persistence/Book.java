package com.hazelcast.hibernate.springhibernate2lc.persistence;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

// tag::doc-cachable[]
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Book {
// end::doc-cachable[]

    @Id
    @GeneratedValue
    private long id;

    private String name;

    public Book() {
    }

    public Book(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Book{" +
          "id=" + id +
          ", name='" + name + '\'' +
          '}';
    }
}