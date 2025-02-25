package com.hazelcast;

import java.util.Random;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class Main {
    public static void main(String[] args) throws Exception {
        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().addAddress("<EXTERNAL-IP>")
                .setSmartRouting(false);
        HazelcastInstance client = HazelcastClient.newHazelcastClient(config);

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
