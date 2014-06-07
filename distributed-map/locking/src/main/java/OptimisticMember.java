import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.io.Serializable;

//This code is broken on purpose.
public class OptimisticMember {
    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<String, Value> map = hz.getMap("map");
        String key = "1";
        map.put(key, new Value());
        System.out.println("Starting");
        for (int k = 0; k < 1000; k++) {
            if (k % 10 == 0) System.out.println("At: " + k);
            for (; ; ) {
                Value oldValue = map.get(key);
                Value newValue = new Value(oldValue);
                Thread.sleep(10);
                newValue.amount++;
                if (map.replace(key, oldValue, newValue))
                    break;
            }
        }
        System.out.println("Finished! Result = " + map.get(key).amount);
    }

    static class Value implements Serializable {
        public int amount;

        public Value() {
        }

        public Value(Value that) {
            this.amount = that.amount;
        }

        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Value)) return false;
            Value that = (Value) o;
            return that.amount == this.amount;
        }
    }
}
