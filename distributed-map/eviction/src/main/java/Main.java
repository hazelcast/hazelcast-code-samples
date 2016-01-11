import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        Map<Integer, String> personsMap = hazelcastInstance.getMap("persons");
        // 2 MB
        String person = new String(new char[1000000]);
        Runtime runtime = Runtime.getRuntime();

        int keyCount = 0;
        int mb = 1024 * 1024;

        while (true) {
            personsMap.put(keyCount, person);
            keyCount++;
            System.out.printf("Unique Puts = %s keyCount : Free Memory (MB) = %s\n", keyCount, runtime.freeMemory() / mb);
        }
    }
}
