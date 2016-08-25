import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.concurrent.TimeUnit;

public class NearCacheWithTTL extends NearCacheSupport {

    public static void main(String[] args) throws Exception {
        HazelcastInstance member = Hazelcast.newHazelcastInstance();
        HazelcastInstance client = HazelcastClient.newHazelcastClient();

        IMap<Integer, Article> map = member.getMap("articlesTTL");
        map.put(1, new Article("foo"));

        map = client.getMap("articlesTTL");
        printNearCacheStats(map, "The put(1, article) call on the member has no effect on the Client Near Cache");

        map.get(1);
        printNearCacheStats(map, "The first get(1) call populates the Near Cache");

        map.get(1);
        printNearCacheStats(map, "The second get(1) call is served from the Near Cache");

        TimeUnit.SECONDS.sleep(2);
        System.out.println("We've waited for the time-to-live-seconds, so the Near Cache entry is expired.");

        map.get(1);
        printNearCacheStats(map, "The third get(1) call is fetching the value again from the map");

        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }
}
