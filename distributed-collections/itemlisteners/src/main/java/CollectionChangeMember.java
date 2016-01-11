import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class CollectionChangeMember {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IQueue<String> queue = hz.getQueue("queue");
        queue.put("foo");
        queue.put("bar");
        queue.take();
        queue.take();
    }
}
