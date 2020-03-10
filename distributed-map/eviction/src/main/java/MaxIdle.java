import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MaxIdle {

    public static void main(String[] args)
            throws InterruptedException {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        IMap<Integer, String> map = hazelcastInstance.getMap("default");

        map.put(1, "Number One", 0, SECONDS, 5, SECONDS);
        System.out.println("Entry expiration: " + map.getEntryView(1).getExpirationTime());

        Thread.sleep(SECONDS.toMillis(2));
        // Expiration updated since the entry was touched.
        map.get(1);
        System.out.println("Entry expiration: " + map.getEntryView(1).getExpirationTime());

        // Entry expires due to idleness
        Thread.sleep(SECONDS.toMillis(5));

        if (map.get(1) != null) {
            System.err.println("Entry is still alive!");
            System.exit(-1);
            return;
        }

        System.out.println("Entry expired!");
        System.exit(0);
    }

}
