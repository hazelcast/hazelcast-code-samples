import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.ILock;

public class RaceFreeMember {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IAtomicLong number1 = hz.getAtomicLong("number1");
        IAtomicLong number2 = hz.getAtomicLong("number2");
        ILock lock = hz.getLock("lock");
        System.out.println("Started");

        for (int i = 0; i < 10000; i++) {
            if (i % 100 == 0) {
                System.out.println("at: " + i);
            }
            lock.lock();
            try {
                if (i % 2 == 0) {
                    long n1 = number1.get();
                    Thread.sleep(10);
                    long n2 = number2.get();
                    if (n1 - n2 != 0) {
                        System.out.println("Datarace detected!");
                    }
                } else {
                    number1.incrementAndGet();
                    number2.incrementAndGet();
                }
            } finally {
                lock.unlock();
            }
        }

        System.out.println("Finished");
        System.exit(0);
    }
}
