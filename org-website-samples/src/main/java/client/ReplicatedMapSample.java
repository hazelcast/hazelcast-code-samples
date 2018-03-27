package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ReplicatedMap;

public class ReplicatedMapSample {

    public static void main(String[] args) {
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        // Get a Replicated Map called "my-replicated-map"
        ReplicatedMap<String, String> map = hz.getReplicatedMap("my-replicated-map");
        // Put and Get a value from the Replicated Map
        String replacedValue = map.put("key", "value");
        // key/value replicated to all members
        System.out.println("replacedValue = " + replacedValue);
        // Will be null as its first update
        String value = map.get("key");
        // the value is retrieved from a random member in the cluster
        System.out.println("value for key = " + value);
        // Shutdown this Hazelcast Client
        hz.shutdown();
    }
}
