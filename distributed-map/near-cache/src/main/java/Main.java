import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        Map<Long, Article> map = hz.getMap("articles");

        Article article = new Article("foo");
        map.put(1L, article);

        // the first get() will populate the Near Cache
        Article firstGet = map.get(1L);
        // the second and third get() will be served from the Near Cache
        Article secondGet = map.get(1L);
        Article thirdGet = map.get(1L);

        System.out.println("The first and second article instance will be different: " + (firstGet == secondGet));
        System.out.println("The second and third article instance will be the same: " + (secondGet == thirdGet));

        Hazelcast.shutdownAll();
    }
}
