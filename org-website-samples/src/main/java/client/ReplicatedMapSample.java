package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ReplicatedMap;

public class ReplicatedMapSample {

    public static void main(String[] args) {
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        // Get a Replicated Map called "my-replicated-map"
        ReplicatedMap<String, String> map = hz.getReplicatedMap("my-replicated-map");
        // Put and Get a value from the Replicated Map
        map.put("key", "value"); // key/value replicated to all members
        map.get("key"); // the value retrieved from local member
        // Shutdown the Hazelcast Cluster Member
        hz.shutdown();
    }
}