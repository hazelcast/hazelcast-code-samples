import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class WriteMember {
    public static void main(String[] args) throws InterruptedException {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<Long, Person> personMap = hz.getMap("personMap");
        personMap.put(1L, new Person("Peter"));
        System.exit(0);
    }
}
