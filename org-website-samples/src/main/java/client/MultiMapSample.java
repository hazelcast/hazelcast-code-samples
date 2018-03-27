package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

import java.util.Collection;

public class MultiMapSample {
    public static void main(String[] args) {
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        // Get the Distributed MultiMap from Cluster.
        MultiMap<String, String> multiMap = hz.getMultiMap("my-distributed-multimap");
        // Put values in the map against the same key
        multiMap.put("my-key", "value1");
        multiMap.put("my-key", "value2");
        multiMap.put("my-key", "value3");
        // Print out all the values for associated with key called "my-key"
        Collection<String> values = multiMap.get("my-key");
        System.out.println(values);
        // remove specific key/value pair
        multiMap.remove("my-key", "value2");
        // Shutdown this Hazelcast Client
        hz.shutdown();
    }
}
