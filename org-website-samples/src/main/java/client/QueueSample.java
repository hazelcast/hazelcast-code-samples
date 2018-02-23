package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class QueueSample {
    public static void main(String[] args) throws InterruptedException {
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        // Get a Blocking Queue called "my-distributed-queue"
        BlockingQueue<String> queue = hz.getQueue("my-distributed-queue");
        // Offer a String into the Distributed Queue
        queue.offer("item");
        // Poll the Distributed Queue and return the String
        String item = queue.poll();
        //Timed blocking Operations
        queue.offer("anotheritem", 500, TimeUnit.MILLISECONDS);
        String anotherItem = queue.poll(5, TimeUnit.SECONDS);
        //Indefinitely blocking Operations
        queue.put("yetanotheritem");
        String yetanother = queue.take();
        // Shutdown the Hazelcast Cluster Member
        hz.shutdown();
    }
}
