package com.hazelcast.spring.jcache;

import java.util.concurrent.TimeUnit;

public class DummyBean implements IDummyBean {

    @Override
    public String getCity() {
        System.out.println("DummyBean.getCity() called!");
        try {
            // emulation of slow method
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Ankara";
    }
}
