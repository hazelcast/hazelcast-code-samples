import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;

import java.util.concurrent.BlockingQueue;

public class Member {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        System.out.println("Hazelcast Member instance is running!");

        BlockingQueue<String> queue = hz.getQueue("queue");
        try {
            for (; ; ) {
                System.out.println(queue.take());
            }
        } catch (HazelcastInstanceNotActiveException e) {
            System.err.println("Unable to take from the queue. Hazelcast Member is probably going down!");
        }
    }
}
