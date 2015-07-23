import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.ringbuffer.OverflowPolicy;
import com.hazelcast.ringbuffer.Ringbuffer;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;

public class Writer {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        Random random = new Random();

        Ringbuffer<Long> rb = hz.getRingbuffer("rb");
        long k = 100;
        while (true) {
            long sleepMs = 100;
            for (; ; ) {
                long result = rb.addAsync(k, OverflowPolicy.FAIL).get();
                if (result != -1) {
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(sleepMs);
                sleepMs = min(5000, sleepMs * 2);
            }

            // add a bit of random delay to make it look a bit more realistic
            Thread.sleep(random.nextInt(10));

            System.out.println("Written: " + k);
            k++;
        }
    }
}
