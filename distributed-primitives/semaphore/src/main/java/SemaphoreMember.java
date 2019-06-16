import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;
import com.hazelcast.cp.ISemaphore;

/**
 * @deprecated {@code HazelcastInstance.getSemaphore()} may lose strong consistency
 * in case of network failures and server failures.
 * Please use {@code HazelcastInstance.getCPSubsystem().getSemaphore()} instead.
 * You can see a code sample for the new impl in the cp-subsystem code samples module.
 */
@Deprecated
public class SemaphoreMember {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        ISemaphore semaphore = hz.getSemaphore("semaphore");
        IAtomicLong resource = hz.getAtomicLong("resource");

        for (int i = 0; i < 1000; i++) {
            System.out.println("At iteration: " + i + ", Active Threads: " + resource.get());
            semaphore.acquire();
            try {
                resource.incrementAndGet();
                Thread.sleep(1000);
                resource.decrementAndGet();
            } finally {
                semaphore.release();
            }
        }
        System.out.println("Finished");
    }
}
