import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MaxIdleAndTtl {

    public static void main(String[] args)
            throws InterruptedException {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        Map<Integer, String> map = hazelcastInstance.getMap("default");

        ((IMap<Integer, String>) map).put(1, "Number One", 5, SECONDS, 2, SECONDS);
        // Expiration is set to the closest preference, in this case 2 seconds due to MaxIdle
        System.out.println("Entry expiration: " + ((IMap<Integer, String>) map).getEntryView(1).getExpirationTime());

        // Touching the record moves the expiration by 1 second until the TTL time is preferred because it comes sooner.
        Thread.sleep(SECONDS.toMillis(1));
        map.get(1);
        System.out.println("Entry expiration: " + ((IMap<Integer, String>) map).getEntryView(1).getExpirationTime());

        Thread.sleep(SECONDS.toMillis(1));
        map.get(1);
        System.out.println("Entry expiration: " + ((IMap<Integer, String>) map).getEntryView(1).getExpirationTime());

        Thread.sleep(SECONDS.toMillis(1));
        map.get(1);
        System.out.println("Entry expiration: " + ((IMap<Integer, String>) map).getEntryView(1).getExpirationTime());

        Thread.sleep(SECONDS.toMillis(1));
        map.get(1);
        System.out.println("Entry expiration: " + ((IMap<Integer, String>) map).getEntryView(1).getExpirationTime());

        Thread.sleep(SECONDS.toMillis(1));
        if (map.get(1) == null) {
            System.out.println("Entry expired due to TTL.");
        }

        System.exit(0);
    }

}
