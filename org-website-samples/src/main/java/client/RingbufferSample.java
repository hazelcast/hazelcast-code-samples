package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.ringbuffer.Ringbuffer;

public class RingbufferSample {

    public static void main(String[] args) throws InterruptedException {
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        // Get a Ringbuffer called "my-ringbuffer"
        final Ringbuffer<Integer> rb = hz.getRingbuffer("my-ringbuffer");
        // Add some integers as values to the ringbuffer
        for(int k=0;k<100;k++){
            rb.add(k);
        }
        System.out.println(rb.headSequence());
        System.out.println(rb.tailSequence());
        Integer value = rb.readOne(0);
        System.out.println(value);
    }


}