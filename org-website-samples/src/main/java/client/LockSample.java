package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

import java.util.concurrent.locks.Lock;

public class LockSample {
    public static void main(String[] args) {
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        // Get a distributed lock called "my-distributed-lock"
        Lock lock = hz.getLock("my-distributed-lock");
        // Now create a lock and execute some guarded code.
        lock.lock();
        try {
            //do something here
        } finally {
            lock.unlock();
        }
        // Shutdown this Hazelcast Client
        hz.shutdown();
    }
}
