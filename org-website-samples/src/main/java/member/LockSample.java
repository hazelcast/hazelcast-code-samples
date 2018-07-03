package member;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.concurrent.locks.Lock;

public class LockSample {
    public static void main(String[] args) {
        // Start the Embedded Hazelcast Cluster Member.
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        // Get a distributed lock called "my-distributed-lock"
        Lock lock = hz.getLock("my-distributed-lock");
        // Now create a lock and execute some guarded code.
        lock.lock();
        try {
            //do something here
        } finally {
            lock.unlock();
        }
        // Shutdown this Hazelcast Cluster Member
        hz.shutdown();
    }
}
