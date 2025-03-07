package com.operator.tutorial.mongodb;

import java.io.Serializable;

public class Supplement implements Serializable {

    private final String name;
    private final Integer price;

    public Supplement(String name, Integer price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return price;
    }
}