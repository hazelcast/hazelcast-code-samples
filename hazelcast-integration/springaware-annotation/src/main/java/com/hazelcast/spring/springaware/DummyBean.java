package com.hazelcast.spring.springaware;

public class DummyBean implements IDummyBean {

    @Override
    public String getCity() {
        System.out.println("DummyBean.getCity() called!");
        return "Istanbul";
    }
}
