package com.hazelcast.namespaces.staticconfig;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.EntryProcessor;
import usercodenamespaces.IncrementingEntryProcessor;

public class Client {

    public static void main(String[] args) {
        HazelcastInstance client = HazelcastClient.newHazelcastClient();

        EntryProcessor entryProcessor = new IncrementingEntryProcessor();
        client.getMap("map1").executeOnKey("key", entryProcessor);

        //will increment the value from the map and print it.
        System.out.println(client.getMap("map1").get("key"));
    }
}
