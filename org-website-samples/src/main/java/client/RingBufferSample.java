package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.ringbuffer.Ringbuffer;

public class RingBufferSample {
    public static void main(String[] args) throws Exception {
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        Ringbuffer<Long> rb = hz.getRingbuffer("rb");
        // add two items into ring buffer
        rb.add(100L);
        rb.add(200L);
        // we start from the oldest item.
        // if you want to start from the next item, call rb.tailSequence()+1
        long sequence = rb.headSequence();
        System.out.println(rb.readOne(sequence));
        sequence++;
        System.out.println(rb.readOne(sequence));
        // Shutdown this Hazelcast Client
        hz.shutdown();
    }
}
