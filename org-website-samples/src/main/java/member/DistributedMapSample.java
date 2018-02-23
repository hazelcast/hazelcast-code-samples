package member;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * Demonstrates starting a Hazelcast Cluster Member and interacting with a Distributed Map.
 */

    public class DistributedMapSample {
        public static void main(String[] args) {
            // Start the Embedded Hazelcast Cluster Member.
            HazelcastInstance hz = Hazelcast.newHazelcastInstance();
            // Get the Distributed Map from Cluster.
            IMap<String, String> map = hz.getMap("my-distributed-map");
            //Standard Put and Get.
            map.put("key", "value");
            map.get("key");
            //Concurrent Map methods, optimistic updating
            map.putIfAbsent("somekey", "somevalue");
            map.replace("key", "value", "newvalue");
            // Shutdown the Hazelcast Cluster Member
            hz.shutdown();
        }
    }

