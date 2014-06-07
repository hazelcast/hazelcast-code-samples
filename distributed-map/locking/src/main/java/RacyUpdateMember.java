import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.io.Serializable;

public class RacyUpdateMember {
    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<String, Value> map = hz.getMap("map");
        String key = "1";
        map.put(key, new Value());
        System.out.println("Starting");
        for (int k = 0; k < 1000; k++) {
            if (k % 100 == 0) System.out.println("At: " + k);
            Value value = map.get(key);
            Thread.sleep(10);
            value.amount++;
            map.put(key, value);
        }
        System.out.println("Finished! Result = " + map.get(key).amount);
    }

    static class Value implements Serializable {
        public int amount;
    }
}
