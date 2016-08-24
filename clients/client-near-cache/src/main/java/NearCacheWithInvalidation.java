import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class NearCacheWithInvalidation extends NearCacheSupport {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        HazelcastInstance client1 = HazelcastClient.newHazelcastClient();
        HazelcastInstance client2 = HazelcastClient.newHazelcastClient();

        IMap<String, Article> map1 = client1.getMap("articlesInvalidation");
        IMap<String, Article> map2 = client2.getMap("articlesInvalidation");

        String key = generateKeyOwnedBy(hz);

        map2.put(key, new Article("foo"));
        printNearCacheStats(map1, "The map2.put(key, new Article(\"foo\")) call has no effect on the Near Cache of map1");

        map1.get(key);
        printNearCacheStats(map1, "The first map1.get(key) call populates the Near Cache");

        map2.put(key, new Article("bar"));
        printNearCacheStats(map1, "The map2.put(key, new Article(\"bar\")) call will invalidate the Near Cache on map1");

        waitForInvalidationEvents();
        printNearCacheStats(map1, "The Near Cache of map1 is empty after the invalidation event has been processed");

        map1.get(key);
        printNearCacheStats(map1, "The next map1.get(key) call populates the Near Cache again");

        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }
}
