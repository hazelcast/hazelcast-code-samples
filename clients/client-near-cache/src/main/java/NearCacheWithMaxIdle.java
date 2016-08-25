import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.concurrent.TimeUnit;

public class NearCacheWithMaxIdle extends NearCacheSupport {

    public static void main(String[] args) throws Exception {
        HazelcastInstance member = Hazelcast.newHazelcastInstance();
        HazelcastInstance client = HazelcastClient.newHazelcastClient();

        IMap<Integer, Article> map = member.getMap("articlesMaxIdle");
        map.put(1, new Article("foo"));

        map = client.getMap("articlesMaxIdle");
        printNearCacheStats(map, "The put(1, article) call on the member has no effect on the Client Near Cache");

        map.get(1);
        printNearCacheStats(map, "The first get(1) call populates the Near Cache");

        // with this short sleep time, the Near Cache entry should not expire
        for (int i = 0; i < 20; i++) {
            map.get(1);
            TimeUnit.MILLISECONDS.sleep(100);
        }
        printNearCacheStats(map, "We have called get(1) every 100 ms, so the Near cache entry could not expire");

        TimeUnit.SECONDS.sleep(2);
        System.out.println("We've waited for max-idle-seconds, so the Near Cache entry is expired.");

        map.get(1);
        printNearCacheStats(map, "The next get(1) call is fetching the value again from the map");

        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }
}
