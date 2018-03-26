package member;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ReplicatedMap;

public class ReplicatedMapSample {
    public static void main(String[] args) {
        // Start the Embedded Hazelcast Cluster Member.
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        // Get a Replicated Map called "my-replicated-map"
        ReplicatedMap<String, String> map = hz.getReplicatedMap("my-replicated-map");
        // Put and Get a value from the Replicated Map
        map.put("key", "value"); // key/value replicated to all members
        map.get("key"); // the value retrieved from local member
        // Shutdown the Hazelcast Cluster Member
        hz.shutdown();
    }
}