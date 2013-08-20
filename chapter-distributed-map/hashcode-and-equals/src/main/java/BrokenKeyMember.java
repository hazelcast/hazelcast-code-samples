import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.HashMap;
import java.util.Map;

public class BrokenKeyMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        Map<Pair, String> normalMap = new HashMap<>();
        Map<Pair, String> hzMap = hz.getMap("map");

        Pair key1 = new Pair("a", "b");
        Pair key2 = new Pair("a", "c");

        normalMap.put(key1, "foo");
        hzMap.put(key1, "foo");

        System.out.println("normalMap.get: " + normalMap.get(key2));
        System.out.println("hzMap.get: " + hzMap.get(key2));

        System.exit(0);
    }
}
