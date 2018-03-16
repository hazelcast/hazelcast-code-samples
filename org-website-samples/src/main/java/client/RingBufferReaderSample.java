package client;

import com.hazelcast.config.Config;
import com.hazelcast.config.RingbufferConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.ringbuffer.Ringbuffer;

public class RingBufferReaderSample {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        Config config = new Config();
        RingbufferConfig ringbufferConfig = new RingbufferConfig();
        ringbufferConfig.setName("rb");
        ringbufferConfig.setCapacity(1000);
        config.addRingBufferConfig(ringbufferConfig);

        Ringbuffer<Long> rb = hz.getRingbuffer("rb");
        // we start from the oldest item.
        // if you want to start from the next item, call rb.tailSequence()+1
        long sequence = rb.headSequence();
        System.out.println("Start reading from: " + sequence);
        while (true) {
            long item = rb.readOne(sequence);
            sequence++;
            System.out.println("Read: " + item);
        }
    }
}
