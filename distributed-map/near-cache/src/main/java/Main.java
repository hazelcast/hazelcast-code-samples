import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        Map<Long, Article> articles = hz.getMap("articles");
        Article article = new Article("foo");
        articles.put(1L, article);

        Article found1 = articles.get(1L);
        Article found2 = articles.get(1L);
        System.out.println("found == article: " + (found1 == found2));
    }
}
