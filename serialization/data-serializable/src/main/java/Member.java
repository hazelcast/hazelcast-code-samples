import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

public class Member {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        Map<String, Person> map = hz.getMap("map");

        map.put("Peter", new Person("Peter"));

        Person person = map.get("Peter");
        System.out.println(person);

        Hazelcast.shutdownAll();
    }
}
