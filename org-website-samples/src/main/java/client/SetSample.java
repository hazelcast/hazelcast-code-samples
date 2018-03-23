package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

import java.util.Set;

public class SetSample {
    public static void main(String[] args) {
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        // Get the Distributed Set from Cluster.
        Set<String> set = hz.getSet("my-distributed-set");
        // Add items to the set with duplicates
        set.add("item1");
        set.add("item1");
        set.add("item2");
        set.add("item2");
        set.add("item2");
        set.add("item3");
        // Get the items. Note that there are no duplicates.
        for (String item: set) {
            System.out.println(item);
        }
        // Shutdown this Hazelcast client
        hz.shutdown();
    }
}

