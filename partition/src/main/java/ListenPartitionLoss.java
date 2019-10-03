import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class ListenPartitionLoss {

    public static void main(String[] args) {
        Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().addMember("127.0.0.1").setEnabled(true);

        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance instance3 = Hazelcast.newHazelcastInstance(config);

        IMap<Object, Object> map = instance1.getMap("test-map");
        // initialize partition assignments
        map.size();

        instance2.getPartitionService().addPartitionLostListener(new LoggingPartitionLostListener());

        instance1.getLifecycleService().terminate();
        instance3.getLifecycleService().terminate();
    }
}
