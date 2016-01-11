import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class MultipleMembers {

    public static void main(String[] args) {
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance();
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance();
    }
}
