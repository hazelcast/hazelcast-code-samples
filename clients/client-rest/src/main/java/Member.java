import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class Member {

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.setProperty("hazelcast.rest.enabled", "true");
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        Person person = new Person("Joe");

        IMap<String, String> hzSimpleMap = hz.getMap("simple");
        hzSimpleMap.set("key1", "value1");

        IMap<String, Person> hzObjectMap = hz.getMap("object");
        hzObjectMap.set("key1", person);
    }
}
