import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.QuorumConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.quorum.QuorumException;

public class ClusterQuorum {

    private static final String NAME = "AT_LEAST_TWO_NODES";

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

        IMap<String, String> map = instance1.getMap(NAME);

        // Quorum will succeed
        System.out.println("Quorum is satisfied, so the following put will throw no exception");
        map.put("key1", "we have the quorum");

        String value = map.get("key1");
        System.out.println("'key1' has the value '" + value + "'");

        // Quorum will fail
        System.out.println("Shutdown one instance, so there won't be enough members for quorum presence");
        instance2.getLifecycleService().shutdown();

        System.out.println("The following put operation will fail");
        try {
            map.put("key2", "will not succeed");
        } catch (QuorumException expected) {
            System.out.println("Put operation failed with expected QuorumException: " + expected.getMessage());
        }

        Hazelcast.shutdownAll();
    }
}
