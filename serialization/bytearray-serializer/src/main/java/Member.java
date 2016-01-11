import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

public class Member {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        Map<String, Person> map = hz.getMap("map");

        map.put("foo", new Person("foo"));
        System.out.println("finished writing");

        System.out.println(map.get("foo"));
        System.out.println("finished reading");

        Hazelcast.shutdownAll();
    }
}
