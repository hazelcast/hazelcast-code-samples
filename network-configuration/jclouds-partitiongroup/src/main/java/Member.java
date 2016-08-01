import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.PartitionGroupConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

public class Member {

    public static void main(String[] args) throws InterruptedException {
        Config cfg = new ClasspathXmlConfig("hazelcast.xml");
        Hazelcast.newHazelcastInstance(cfg);
        Hazelcast.newHazelcastInstance(cfg);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(cfg);
        Map map = hz.getMap("example");
        for (int i = 0; i < 30000; i++) {
            if (i % 1000 == 0) {
                System.out.println("Adding " + i + "th element to map");
            }
            map.put(Math.random(), i);
        }
        while (true) {
            Thread.sleep(500);
            System.out.println(hz.getPartitionService().isClusterSafe());
            System.out.println(hz.getMap("example").size());
        }
    }
}
