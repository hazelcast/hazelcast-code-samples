package member;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class QueueSample {
    public static void main(String[] args) throws InterruptedException {
        // Start the Embedded Hazelcast Cluster Member.
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        // Get a Blocking Queue called "my-distributed-queue"
        BlockingQueue<String> queue = hz.getQueue("my-distributed-queue");
        // Offer a String into the Distributed Queue
        queue.offer("item");
        // Poll the Distributed Queue and return the String
        queue.poll();
        //Timed blocking Operations
        queue.offer("anotheritem", 500, TimeUnit.MILLISECONDS);
        queue.poll(5, TimeUnit.SECONDS);
        //Indefinitely blocking Operations
        queue.put("yetanotheritem");
        System.out.println(queue.take());
        // Shutdown the Hazelcast Cluster Member
        hz.shutdown();
    }
}
