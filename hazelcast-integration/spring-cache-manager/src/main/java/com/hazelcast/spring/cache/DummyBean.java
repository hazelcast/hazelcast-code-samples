package com.hazelcast.spring.cache;

public class DummyBean implements IDummyBean {

    @Override
    public String getCity() {
        System.out.println("getCity called");
        return "Istanbul";
    }
}
