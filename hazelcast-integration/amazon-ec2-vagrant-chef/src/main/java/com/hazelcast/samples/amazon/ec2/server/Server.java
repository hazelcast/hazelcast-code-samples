package com.hazelcast.samples.amazon.ec2.server;

import com.hazelcast.core.Hazelcast;

public class Server {

    public static void main(String[] args) {
        Hazelcast.newHazelcastInstance();
    }
}
