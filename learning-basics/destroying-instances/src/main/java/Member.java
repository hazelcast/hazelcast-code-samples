import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class Member {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance();
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance();

        IQueue<String> q1 = hz1.getQueue("q");
        IQueue<String> q2 = hz2.getQueue("q");

        q1.add("foo");
        System.out.println("q1.size: " + q1.size() + " q2.size:" + q2.size());

        q1.destroy();
        System.out.println("q1.size: " + q1.size() + " q2.size:" + q2.size());
    }
}
