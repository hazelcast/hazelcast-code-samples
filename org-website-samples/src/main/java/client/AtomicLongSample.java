package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

public class AtomicLongSample {
    public static void main(String[] args) {
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        // Get an Atomic Counter, we'll call it "counter"
        IAtomicLong counter = hz.getAtomicLong("counter");
        // Add and Get the "counter"
        counter.addAndGet(3);
        // value is now 3
        // Display the "counter" value
        System.out.println("counter: " + counter.get());
        // Shutdown this Hazelcast Client
        hz.shutdown();
    }
}
