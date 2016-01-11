import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

import java.util.Collection;

public class PrintMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        MultiMap<String, String> map = hz.getMultiMap("map");
        for (String key : map.keySet()) {
            Collection<String> values = map.get(key);
            System.out.printf("%s -> %s\n", key, values);
        }
    }
}
