import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ModifyMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<String, String> map = hz.getMap("someMap");
        String key = "" + System.nanoTime();
        String value = "1";
        map.put(key, value);
        map.put(key, "2");
        map.delete(key);

        System.exit(0);
    }
}
