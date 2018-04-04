package member;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IFunction;

public class AtomicLongSample {
    public static class MultiplyByTwo implements IFunction<Long, Long> {
        @Override
        public Long apply(Long input) {
            return input * 2;
        }
    }

    public static void main(String[] args) {
        // Start the Embedded Hazelcast Cluster Member.
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        // Get an Atomic Counter, we'll call it "counter"
        IAtomicLong counter = hz.getAtomicLong("counter");
        // Add and Get the "counter"
        counter.addAndGet(3);
        // value is now 3
        // Multiply the "counter" by passing it an IFunction
        counter.alter(new MultiplyByTwo());
        //value is now 6
        // Display the "counter" value
        System.out.println("counter: " + counter.get());
        // Shutdown this Hazelcast Cluster Member
        hz.shutdown();
    }
}
