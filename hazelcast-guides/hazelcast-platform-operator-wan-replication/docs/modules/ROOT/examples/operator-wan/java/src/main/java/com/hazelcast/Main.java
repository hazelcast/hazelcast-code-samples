package com.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.Random;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            System.out.println("You need to pass two arguments. The first argument must be `fill` or `size`. The second argument must be `mapName`.");
        } else if (!((args[0].equals("fill") || args[0].equals("size")))) {
            System.out.println("Wrong argument, you should pass: fill or size");
        } else{
            ClientConfig config = new ClientConfig();
            config.getNetworkConfig().addAddress("<EXTERNAL-IP>");

            HazelcastInstance client = HazelcastClient.newHazelcastClient(config);
            System.out.println("Successful connection!");

            String mapName = args[1];
            IMap<String, String> map = client.getMap(mapName);

            if (args[0].equals("fill")) {
                System.out.printf("Starting to fill the map (%s) with random entries.\n", mapName);

                Random random = new Random();
                while (true) {
                    int randomKey = random.nextInt(100_000);
                    map.put("key-" + randomKey, "value-" + randomKey);
                    System.out.println("Current map size: " + map.size());
                }
            } else {
                System.out.printf("The map (%s) size: (%d)\n\n", mapName, map.size());
                client.shutdown();
            }
        }

    }
}
