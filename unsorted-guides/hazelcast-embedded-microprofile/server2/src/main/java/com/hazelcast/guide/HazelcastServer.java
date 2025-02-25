package com.hazelcast.guide;

import com.hazelcast.core.Hazelcast;

public class HazelcastServer {

    public static void main(String[] args) {
        // Creates a standalone Hazelcast member.
        // Useful for providing a Hazelcast cluster
        // to test client/server mode.
        Hazelcast.newHazelcastInstance();
    }

}
