import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

public class FillMapMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        Map<String, String> map = hz.getReplicatedMap("map");

        map.put("1", "Tokyo");
        map.put("2", "Paris");
        map.put("3", "New York");

        System.out.println("Finished loading map");
        hz.shutdown();
    }
}
