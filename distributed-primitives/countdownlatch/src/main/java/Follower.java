import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.ICountDownLatch;

import java.util.concurrent.TimeUnit;

/**
 * @deprecated {@code HazelcastInstance.getCountDownLatch()} may lose strong consistency
 * in case of network failures and server failures.
 * Please use {@code HazelcastInstance.getCPSubsystem().getCountDownLatch()} instead.
 * You can see a code sample for the new impl in the cp-subsystem code samples module.
 */
@Deprecated
public class Follower {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        ICountDownLatch latch = hz.getCountDownLatch("countDownLatch");

        System.out.println("Waiting");
        boolean success = latch.await(10, TimeUnit.SECONDS);

        System.out.println("Complete: " + success);
    }
}
