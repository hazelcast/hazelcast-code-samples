import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import data.Person;

public class WriteMember {

    public static void main(String[] args) throws InterruptedException {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<Long, Person> personMap = hz.getMap("personMap");
        personMap.put(1L, new Person(1L, "Peter"));
        hz.shutdown();
    }
}
