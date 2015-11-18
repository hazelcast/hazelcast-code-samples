package com.hazelcast.spring.jcache;

public class DummyBean implements IDummyBean {

    @Override
    public String getCity() {
        System.out.println("getCity() called!");
        return "Ankara";
    }
}
