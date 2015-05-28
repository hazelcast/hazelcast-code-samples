import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.instance.HazelcastInstanceFactory;
import com.hazelcast.map.MapPartitionLostEvent;
import com.hazelcast.map.listener.MapPartitionLostListener;

public class ListenMapPartitionLostEvents {

    public static void main(String[] args) {
        final Config config = new Config();
        config.getMapConfig("map0").setBackupCount(0); // might lose data if any node crashes
        config.getMapConfig("map1").setBackupCount(1); // keeps its data if a single node crashes

        final HazelcastInstance instance1 = HazelcastInstanceFactory.newHazelcastInstance(config);
        final HazelcastInstance instance2 = HazelcastInstanceFactory.newHazelcastInstance(config);

        final IMap<Object, Object> map0 = instance1.getMap("map0");
        map0.put(0, 0);

        map0.addPartitionLostListener(new MapPartitionLostListener() {
            @Override
            public void partitionLost(MapPartitionLostEvent event) {
                System.out.println(event);
            }
        });

        final IMap<Object, Object> map1 = instance1.getMap("map1");
        map1.addPartitionLostListener(new MapPartitionLostListener() {
            @Override
            public void partitionLost(MapPartitionLostEvent event) {
                System.out.println("This line will not be printed! " + event);
            }
        });

        instance2.getLifecycleService().terminate();
    }
}
