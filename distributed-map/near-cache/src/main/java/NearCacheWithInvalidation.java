import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class NearCacheWithInvalidation extends NearCacheSupport {

    public static void main(String[] args) {
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance();
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance();

        IMap<String, Article> map1 = hz1.getMap("articlesInvalidation");
        IMap<String, Article> map2 = hz2.getMap("articlesInvalidation");

        String key = generateKeyOwnedBy(hz2);

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

        Hazelcast.shutdownAll();
    }
}
