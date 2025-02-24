import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class SumTask implements Callable<Integer>, Serializable, HazelcastInstanceAware {

    private transient HazelcastInstance hz;

    @Override
    public void setHazelcastInstance(HazelcastInstance hz) {
        this.hz = hz;
    }

    @Override
    public Integer call() throws Exception {
        IMap<String, Integer> map = hz.getMap("map");
        int result = 0;
        for (String key : map.localKeySet()) {
            System.out.println("Calculating for key: " + key);
            result += map.get(key);
        }
        System.out.println("Local result: " + result);
        return result;
    }
}
