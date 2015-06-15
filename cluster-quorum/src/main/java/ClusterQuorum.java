import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.QuorumConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ClusterQuorum {

    public static final String NAME = "AT_LEAST_TWO_NODES";

    public static void main(String[] args) {
        QuorumConfig quorumConfig = new QuorumConfig();
        quorumConfig.setName(NAME).setEnabled(true).setSize(2);

        MapConfig mapConfig = new MapConfig();
        mapConfig.setName(NAME).setQuorumName(NAME);

        Config config = new Config();
        config.addMapConfig(mapConfig);
        config.addQuorumConfig(quorumConfig);


        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(config);

        IMap<Object, Object> map = instance1.getMap(NAME);
        map.put("key", "value");

        System.out.println("Quorum is satisfied and key/value put into the map without problem");

        System.out.println("Now killing one instance, and there won't be enough members for quorum presence");
        instance2.getLifecycleService().shutdown();

        System.out.println("Following put operation will fail");
        try {
            map.put("key2", "value2");
        } catch (Exception e) {
            System.out.println("Put operation failed with exception -> " + e.getMessage());
        }
        instance1.shutdown();
        instance2.shutdown();
    }
}
