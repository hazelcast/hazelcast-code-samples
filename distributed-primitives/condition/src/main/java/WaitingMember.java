import com.hazelcast.core.*;

public class WaitingMember {
    public static void main(String[] args) throws InterruptedException {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IAtomicLong counter = hz.getAtomicLong("counter");
        ILock lock = hz.getLock("lock");
        ICondition isOneCondition = lock.newCondition("isOne");

        lock.lock();
        try {
            while (counter.get() != 1) {
                System.out.println("Waiting");
                isOneCondition.await();
            }
        } finally {
            lock.unlock();
        }
        System.out.println("Wait finished, counter: " + counter.get());
    }
}