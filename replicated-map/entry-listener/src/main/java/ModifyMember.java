import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ReplicatedMap;

public class ModifyMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        ReplicatedMap<String, String> map = hz.getReplicatedMap("somemap");

        String key = "" + System.nanoTime();
        String value = "1";
        map.put(key, value);
        map.put(key, "2");
        map.remove(key);

        hz.shutdown();
    }
}
