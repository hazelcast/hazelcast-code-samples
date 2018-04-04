package member;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Set;

public class SetSample {
    public static void main(String[] args) {
        // Start the Embedded Hazelcast Cluster Member.
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
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
        // Shutdown the Hazelcast Cluster Member
        hz.shutdown();
    }
}

