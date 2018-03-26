package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

import java.util.List;

public class ListSample {
    public static void main(String[] args) {
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        // Get the Distributed List from Cluster.
        List<String> list = hz.getList("my-distributed-list");
        // Add elements to the list
        list.add("item1");
        list.add("item2");
        // Remove the first element
        System.out.println("Removed: " + list.remove(0));
        // There is only one element left
        System.out.println("Current size is " + list.size());
        // Clear the list
        list.clear();
        // Shutdown this Hazelcast client
        hz.shutdown();
    }
}

