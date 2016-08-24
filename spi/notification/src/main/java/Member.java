import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class Member {

    public static void main(String[] args) throws Exception {
        final HazelcastInstance[] instances = new HazelcastInstance[2];
        for (int i = 0; i < instances.length; i++) {
            HazelcastInstance instance = Hazelcast.newHazelcastInstance();
            instances[i] = instance;
        }

        System.out.println("Increase the Counter");
        Counter counter = instances[0].getDistributedObject(CounterService.NAME, "myCounter");
        counter.inc(10);

        counter = instances[1].getDistributedObject(CounterService.NAME, "myCounter");
        counter.await(10);
        System.out.println("Finished");

        Hazelcast.shutdownAll();
    }
}
