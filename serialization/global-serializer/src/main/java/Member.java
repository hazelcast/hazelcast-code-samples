import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

public class Member {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        Map<String, Person> map = hz.getMap("map");

        Person person = new Person("peter");
        map.put(person.getName(), person);

        System.out.println(map.get(person.getName()));

        Hazelcast.shutdownAll();
    }
}
