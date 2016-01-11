import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class Member {

    public static void main(String[] args) throws Exception {
        HazelcastInstance[] instances = new HazelcastInstance[3];
        for (int i = 0; i < instances.length; i++) {
            instances[i] = Hazelcast.newHazelcastInstance();
        }

        Counter[] counters = new Counter[4];
        for (int i = 0; i < counters.length; i++) {
            counters[i] = instances[0].getDistributedObject(CounterService.NAME, i + "counter");
        }
        for (Counter counter : counters) {
            System.out.println(counter.inc(1));
        }

        Thread.sleep(10000);

        System.out.println("Creating new members");
        for (int i = 0; i < 3; i++) {
            Hazelcast.newHazelcastInstance();
        }

        Thread.sleep(10000);

        for (Counter counter : counters) {
            System.out.println(counter.inc(1));
        }

        System.out.println("Finished");
        Hazelcast.shutdownAll();
    }
}
