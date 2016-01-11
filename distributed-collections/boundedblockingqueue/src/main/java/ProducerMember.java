import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class ProducerMember {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IQueue<Integer> queue = hz.getQueue("queue");
        for (int i = 1; i < 1000; i++) {
            queue.put(200 + i);
            System.out.println("Producing: " + i);
            Thread.sleep(1000);
        }
        queue.put(-1);
        System.out.println("Producer Finished");
    }
}
