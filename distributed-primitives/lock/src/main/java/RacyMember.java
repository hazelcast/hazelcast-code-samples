import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

public class RacyMember {

    public static void main(String[] args) throws InterruptedException {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IAtomicLong number1 = hz.getAtomicLong("number1");
        IAtomicLong number2 = hz.getAtomicLong("number2");
        System.out.println("Started");

        for (int i = 0; i < 1000000; i++) {
            if (i % 10000 == 0) {
                System.out.println("at: " + i);
            }
            if (i % 2 == 0) {
                long n1 = number1.get();
                Thread.sleep(100);
                long n2 = number2.get();
                if (n1 - n2 != 0) {
                    System.out.println("Difference detected!");
                }
            } else {
                number1.incrementAndGet();
                number2.incrementAndGet();
            }
        }

        System.out.println("Finished");
        System.exit(0);
    }
}
