package client;

import com.hazelcast.config.Config;
import com.hazelcast.config.RingbufferConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.ringbuffer.OverflowPolicy;
import com.hazelcast.ringbuffer.Ringbuffer;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;

public class RingBufferWriterSample {

    public static void main(String[] args) throws Exception {
        Random random = new Random();

        Config config = new Config();
        RingbufferConfig ringbufferConfig = new RingbufferConfig();
        ringbufferConfig.setName("rb");
        ringbufferConfig.setCapacity(1000);
        config.addRingBufferConfig(ringbufferConfig);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        Ringbuffer<Long> rb = hz.getRingbuffer("rb");

        long i = 100;
        while (true) {
            long sleepMs = 100;
            for (; ; ) {
                long result = rb.addAsync(i, OverflowPolicy.FAIL).get();
                if (result != -1) {
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(sleepMs);
                sleepMs = min(5000, sleepMs * 2);
            }

            // add a bit of random delay to make it look a bit more realistic
            Thread.sleep(random.nextInt(10));

            System.out.println("Written: " + i);
            i++;
        }
    }
}
