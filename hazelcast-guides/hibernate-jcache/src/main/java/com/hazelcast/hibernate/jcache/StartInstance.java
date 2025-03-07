package com.hazelcast.hibernate.jcache;

import com.hazelcast.core.Hazelcast;

public class StartInstance {

    // If L2C is used with Hazelcast client, then a running Hazelcast member
    // is needed. This class simply provides an instance.
    public static void main(String[] args) {
        Hazelcast.newHazelcastInstance();
    }
}
