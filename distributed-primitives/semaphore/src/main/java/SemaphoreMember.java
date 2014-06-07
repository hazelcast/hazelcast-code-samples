import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.ISemaphore;

public class SemaphoreMember {
    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        ISemaphore semaphore = hz.getSemaphore("semaphore");
        IAtomicLong resource = hz.getAtomicLong("resource");
        for (int k = 0; k < 1000; k++) {
            System.out.println("At iteration: " + k + ", Active Threads: " + resource.get());
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
