import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class ComputationHeavyTask implements Callable<Integer>, Serializable, HazelcastInstanceAware {

    private transient HazelcastInstance hz;

    public void setHazelcastInstance(HazelcastInstance hz) {
        this.hz = hz;
    }

    public Integer call() throws Exception {
        System.out.println("Running a computation heavy task on " + hz.getCluster().getLocalMember());
        return 0;
    }
}
