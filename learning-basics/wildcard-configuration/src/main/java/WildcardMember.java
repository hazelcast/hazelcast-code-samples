import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

public class WildcardMember {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        Map<String, String> map1 = hz.getMap("testmap1");
        Map<String, String> map2 = hz.getMap("testmap2");

        map1.put("foo", "foo");
        for (; ; ) {
            System.out.println("size:" + map1.size());
            Thread.sleep(1000);
        }
    }
}
