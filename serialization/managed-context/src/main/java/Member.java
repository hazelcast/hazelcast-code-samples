import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ManagedContext;

import java.io.Serializable;
import java.util.Map;

class DummyObject implements Serializable {
    transient Thread trans = new Thread();
    String ser = "someValue";

    @Override
    public String toString() {
        return "DummyObject{" +
                "ser='" + ser + '\'' +
                ", trans=" + trans +
                '}';
    }
}

class ManagedContextImpl implements ManagedContext {
    @Override
    public Object initialize(Object obj) {
        if (obj instanceof DummyObject) {
            ((DummyObject) obj).trans = new Thread();
        }
        return obj;
    }
}

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
        System.exit(0);
    }
}