import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class Member {
    public static void main(String[] args) {
        HazelcastInstance[] instances = new HazelcastInstance[2];
        for (int k = 0; k < instances.length; k++)
            instances[k] = Hazelcast.newHazelcastInstance();

        Counter[] counters = new Counter[4];
        for (int k = 0; k < counters.length; k++) {
            Counter counter = instances[0].getDistributedObject(CounterService.NAME, "counter" + k);
            counters[k] = counter;
            System.out.println(counter.inc(1));
        }

        System.out.println("Finished");
    }
}
