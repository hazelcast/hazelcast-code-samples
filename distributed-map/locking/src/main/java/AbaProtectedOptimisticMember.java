import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.io.Serializable;

public class AbaProtectedOptimisticMember {
    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<String, Value> map = hz.getMap("map");
        String key = "1";
        setup(map,key);
        System.out.println("Starting");
        for (int k = 0; k < 1000; k++) {
            for (; ; ) {
                Value oldValue = map.get(key);
                Value newValue = new Value(oldValue);
                Thread.sleep(10);
                newValue.amount++;
                newValue.version++;
                if (map.replace(key, oldValue, newValue))
                    break;
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
        public int version;

        public Value() {
        }

        public Value(Value that) {
            this.amount = that.amount;
            this.version = that.version;
        }

        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Value)) return false;
            Value that = (Value) o;
            return that.amount == this.amount && this.version == that.version;
        }
    }
}
