package com.hazelcast;

import java.util.Random;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.client.impl.connection.tcp.RoutingMode;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class Main {
    public static void main(String[] args) throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        ClientNetworkConfig networkConfig = clientConfig.getNetworkConfig();
        networkConfig.addAddress("<EXTERNAL-IP>");
        networkConfig.getClusterRoutingConfig().setRoutingMode(RoutingMode.SINGLE_MEMBER);
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        System.out.println("Successful connection!");
        System.out.println("Starting to fill the map with random entries.");

        IMap<String, String> map = client.getMap("map");
        Random random = new Random();
        while (true) {
            int randomKey = random.nextInt(100_000);
            map.put("key-" + randomKey, "value-" + randomKey);
            System.out.println("Current map size: " + map.size());
        }
    }
}
