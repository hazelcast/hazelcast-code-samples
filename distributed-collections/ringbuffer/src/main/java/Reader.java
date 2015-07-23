import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.ringbuffer.Ringbuffer;

public class Reader {

    public static void main(String[] args) throws InterruptedException {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        Ringbuffer<Long> rb = hz.getRingbuffer("rb");
        long sequence = rb.headSequence();
        System.out.println("Start reading from: " + sequence);
        while (true) {
            long item = rb.readOne(sequence);
            sequence++;
            System.out.println("Read: " + item);
        }
    }
}
