import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

public class Member {

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.setManagedContext(new ManagedContextImpl());
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

        Map<String, DummyObject> map = hz.getMap("map");
        DummyObject input = new DummyObject();
        System.out.println(input);

        map.put("1", input);
        DummyObject output = map.get("1");
        System.out.println(output);

        Hazelcast.shutdownAll();
    }
}
