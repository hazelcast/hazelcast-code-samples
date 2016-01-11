import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.ISemaphore;

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
