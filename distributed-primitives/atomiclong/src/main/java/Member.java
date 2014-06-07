import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

public class Member {
    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IAtomicLong counter = hz.getAtomicLong("counter");
        for (int k = 0; k < 1000 * 1000; k++) {
            if (k % 500000 == 0) {
                System.out.println("At: " + k);
            }
            counter.incrementAndGet();
        }
        System.out.printf("Count is %s\n", counter.get());
        System.exit(0);
    }
}