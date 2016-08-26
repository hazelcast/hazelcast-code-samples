import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class NearCacheWithMemoryFormatBinary extends NearCacheSupport {

    public static void main(String[] args) {
        HazelcastInstance member = Hazelcast.newHazelcastInstance();
        HazelcastInstance client = HazelcastClient.newHazelcastClient();

        IMap<Integer, Article> map = member.getMap("articlesBinary");
        Article article = new Article("foo");
        map.put(1, article);

        map = client.getMap("articlesBinary");

        // the first get() will populate the Near Cache
        Article firstGet = map.get(1);
        // the second and third get() will be served from the Near Cache
        Article secondGet = map.get(1);
        Article thirdGet = map.get(1);

        printNearCacheStats(map);

        System.out.println("Since we use in-memory format BINARY, the article instances from the Near Cache will be different.");
        System.out.println("Compare first and second article instance: " + (firstGet == secondGet));
        System.out.println("Compare second and third article instance: " + (secondGet == thirdGet));

        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }
}
