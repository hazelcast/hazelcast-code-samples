
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import data.Person;

public class ReadMember {
    public static void main(String[] args) throws InterruptedException {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<Long, Person> personMap = hz.getMap("personMap");
        Person p = personMap.get(1L);
        System.out.println(p);
        hz.shutdown();
    }
}
