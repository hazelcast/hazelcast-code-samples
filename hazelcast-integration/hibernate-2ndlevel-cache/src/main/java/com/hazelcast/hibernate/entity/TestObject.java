package com.hazelcast.hibernate.entity;


import org.hibernate.annotations.CacheConcurrencyStrategy;
import javax.persistence.*;

@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage= CacheConcurrencyStrategy.READ_WRITE)
@Table(name="testObj")
public class TestObject {

    @Id
    @Column(name="id")
    private int id;

    @Column(name="value")
    private int value;

    public TestObject(){}

    public TestObject(int id, int value) {
        this.id = id;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "TestObject{" +
                "id=" + id +
                ", value=" + value +
                '}';
    }
}




