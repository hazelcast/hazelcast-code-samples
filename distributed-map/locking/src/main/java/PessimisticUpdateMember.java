import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.io.Serializable;

public class PessimisticUpdateMember {
    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<String, Value> map = hz.getMap("map");
        String key = "1";
        setup(map, key);
        System.out.println("Starting");
        for (int k = 0; k < 1000; k++) {
            map.lock(key);
            try {
                Value value = map.get(key);
                Thread.sleep(10);
                value.amount++;
                map.put(key, value);
            } finally {
                map.unlock(key);
            }
        }
        System.out.println("Finished! Result = " + map.get(key).amount);
        hz.shutdown();
    }

    public static void setup(IMap<String, Value> map, String key ) {
        map.lock(key);
        Value v = map.get(key);
        if(v == null) {
            map.put(key, new Value());
        }
        map.unlock(key);
    }

    static class Value implements Serializable {
        public int amount;
    }
}
