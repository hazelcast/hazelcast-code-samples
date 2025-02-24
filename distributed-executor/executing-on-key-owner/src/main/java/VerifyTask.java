import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;

import java.io.Serializable;

public class VerifyTask implements Runnable, Serializable, HazelcastInstanceAware {

    private final String key;
    private transient HazelcastInstance hz;

    public VerifyTask(String key) {
        this.key = key;
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hz) {
        this.hz = hz;
    }

    @Override
    public void run() {
        IMap map = hz.getMap("map");
        boolean localKey = map.localKeySet().contains(key);
        System.out.println("Key " + key + " is local: " + localKey);
    }
}
