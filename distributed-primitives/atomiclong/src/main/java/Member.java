import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

import static java.lang.String.format;

public class Member {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IAtomicLong counter = hz.getAtomicLong("counter");

        for (int i = 0; i < 1000 * 1000; i++) {
            if (i % 500000 == 0) {
                System.out.println("At: " + i);
            }
            counter.incrementAndGet();
        }
        System.out.println(format("Count is %s", counter.get()));

        Hazelcast.shutdownAll();
    }
}
