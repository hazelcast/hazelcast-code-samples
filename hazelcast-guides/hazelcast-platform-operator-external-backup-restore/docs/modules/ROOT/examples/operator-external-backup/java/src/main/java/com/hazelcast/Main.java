package com.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.client.impl.connection.tcp.RoutingMode;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.Random;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            System.out.println("You should pass an argument to run: fill or size");
        } else if (!((args[0].equals("fill") || args[0].equals("size")))) {
            System.out.println("Wrong argument, you should pass: fill or size");
        } else{
            ClientConfig clientConfig = new ClientConfig();
            ClientNetworkConfig networkConfig = clientConfig.getNetworkConfig();
            networkConfig.addAddress("<EXTERNAL-IP>");
            networkConfig.getClusterRoutingConfig().setRoutingMode(RoutingMode.SINGLE_MEMBER);
            HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
            System.out.println("Successful connection!");

            IMap<String, String> map = client.getMap("persistent-map");

            if (args[0].equals("fill")) {
                System.out.println("Starting to fill the map with random entries.");

                Random random = new Random();
                while (true) {
                    int randomKey = random.nextInt(100_000);
                    map.put("key-" + randomKey, "value-" + randomKey);
                    System.out.println("Current map size: " + map.size());
                }
            } else {
                System.out.println("Current map size: " + map.size());
                client.shutdown();
            }
        }

    }
}
