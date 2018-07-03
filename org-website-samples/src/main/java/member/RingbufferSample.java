package member;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.ringbuffer.Ringbuffer;

public class RingbufferSample {

    public static void main(String[] args) throws InterruptedException {
        // Start the Embedded Hazelcast Cluster Member.
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
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
        // Shutdown the Hazelcast Cluster Member
        hz.shutdown();
    }
}
