import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BrokenKeyMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        Map<BrokenKey, String> hzmap = hz.getMap("map");
        Map<BrokenKey, String> normalMap = new HashMap<>();

        BrokenKey key1 = new BrokenKey("a", "b");
        BrokenKey key2 = new BrokenKey("a", "c");

        hzmap.put(key1, "foo");
        normalMap.put(key1, "foo");

        System.out.println("HazelcastMap.get: " + hzmap.get(key2));
        System.out.println("NormalMap.get: " + normalMap.get(key2));
        System.exit(0);
    }

    private static class BrokenKey implements Serializable {
        private final String significant;
        private final String insignificant;

        public BrokenKey(String significant, String insignificant) {
            this.significant = significant;
            this.insignificant = insignificant;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BrokenKey brokenKey = (BrokenKey) o;
            if (!significant.equals(brokenKey.significant)) return false;
            return true;
        }

        public int hashCode() {
            return significant.hashCode();
        }
    }
}
