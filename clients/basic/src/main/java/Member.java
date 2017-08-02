import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.concurrent.BlockingQueue;

public class Member {

    private HazelcastInstance hz;

    public Member() {
        hz = Hazelcast.newHazelcastInstance();
        System.out.println("Hazelcast Member instance is running!");
    }

    public String take() throws Exception {
        BlockingQueue<String> queue = hz.getQueue("queue");

        System.out.println("Waiting for an entry be put in.");
        String taken = queue.take();
        System.out.println(taken);

        return taken;
    }

    public static void main(String[] args) throws Exception {
        new Member().take();
        Hazelcast.shutdownAll();
    }
}
