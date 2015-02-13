package com.hazelcast.samples.amazon.ec2.server;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * Created by dbrimley on 23/12/14.
 */
public class Server {

    public static void main(String args[]){
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
    }
}
