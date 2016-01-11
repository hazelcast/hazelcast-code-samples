import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IEnterpriseMap;
import com.hazelcast.map.QueryCache;
import com.hazelcast.query.SqlPredicate;

import java.util.concurrent.TimeUnit;

/**
 * This example demonstrates the simple usage of a continuous-query-cache feature in a peer-to-peer application.
 */
public class Peer2Peer {

    public static void main(String[] args) {
        HazelcastInstance node = Hazelcast.newHazelcastInstance();
        Hazelcast.newHazelcastInstance();

        IEnterpriseMap<Integer, String> map = (IEnterpriseMap) node.getMap("test");
        QueryCache<Integer, String> cache = map.getQueryCache("myCache", new SqlPredicate("__key > 3 and __key < 84"), true);

        for (int i = 0; i < 100; i++) {
            map.put(i, String.valueOf(i));
        }

        while (true) {
            sleepSeconds(1);
            int size = cache.size();
            System.out.println("Continuous query cache size = " + size);
            if (size == 80) {
                break;
            }
        }

        try {
            for (int i = 4; i < 83; i++) {
                String value = cache.get(i);
                String valueExpected = String.valueOf(i);
                if (!valueExpected.equals(value)) {
                    throw new AssertionError(
                            "Unexpected error, values should be equal valueExpected = " + valueExpected + ", value = " + value);
                }
            }

            System.out.println("All expected values are in cache and they equal to the values in underlying map");
        } finally {
            node.shutdown();
        }
    }

    private static void sleepSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
