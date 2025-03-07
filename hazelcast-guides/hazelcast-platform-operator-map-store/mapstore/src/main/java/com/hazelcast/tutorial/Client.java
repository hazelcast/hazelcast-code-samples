package com.hazelcast.tutorial;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.operator.tutorial.mongodb.Supplement;

public class Client {
    public static void main(String[] args) {
        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().addAddress("<EXTERNAL-IP>")
                .setSmartRouting(false);

        HazelcastInstance client = HazelcastClient.newHazelcastClient(config);
        IMap<String, Supplement> supplements = client.getMap("supplements");
        supplements.set("1", new Supplement("bcaa", 10));
        supplements.set("2", new Supplement("protein", 100));
        supplements.set("3", new Supplement("glucosamine", 200));

        System.out.println("Initial map size:");
        System.out.println(supplements.size());

        supplements.evictAll();

        System.out.println("Map size after eviction:");
        System.out.println(supplements.size());

        supplements.loadAll(true);

        System.out.println("Loading entries form the Database...");
        System.out.println("Map size:");
        System.out.println(supplements.size());
    }
}
