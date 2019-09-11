import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.SplitBrainProtectionConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.spi.properties.GroupProperty;
import com.hazelcast.splitbrainprotection.SplitBrainProtectionException;

/**
 * Demonstrates configuration of split-brain protection with
 * a custom {@link com.hazelcast.splitbrainprotection.SplitBrainProtectionFunction}.
 */
public class CustomClusterSplitBrainProtection {

    private static final String NAME = "AT_LEAST_TWO_NODES";

    public static void main(String[] args) throws Exception {
        SplitBrainProtectionConfig splitBrainProtectionConfig = new SplitBrainProtectionConfig();
        splitBrainProtectionConfig.setName(NAME)
                    .setEnabled(true)
                    .setMinimumClusterSize(2)
                    .setFunctionImplementation(new CustomSplitBrainProtectionFunction());

        // instead we could also configure the SplitBrainProtectionConfig by class name; Hazelcast would instantiate the
        // split brain protection function via its default constructor:
        // splitBrainProtectionConfig.setFunctionClassName("CustomSplitBrainProtectionFunction");

        MapConfig mapConfig = new MapConfig();
        mapConfig.setName(NAME).setSplitBrainProtectionName(NAME);

        Config config = new Config();
        config.addMapConfig(mapConfig);
        config.addSplitBrainProtectionConfig(splitBrainProtectionConfig);
        config.setProperty(GroupProperty.HEARTBEAT_INTERVAL_SECONDS.getName(), "1");

        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(config);

        IMap<String, String> map = instance1.getMap(NAME);

        // Split brain protection will succeed
        System.out.println("Split brain protection is satisfied, so the following put will throw no exception");
        map.put("key1", "we have the Split brain protection");

        String value = map.get("key1");
        System.out.println("'key1' has the value '" + value + "'");

        // Split brain protection will fail
        System.out.println("Shutdown one instance, so there won't be enough members for Split brain protection presence");
        instance2.getLifecycleService().shutdown();
        // wait for a moment to detect that cluster fell apart
        Thread.sleep(1000);

        System.out.println("The following put operation will fail");
        try {
            map.put("key2", "will not succeed");
        } catch (SplitBrainProtectionException expected) {
            System.out.println("Put operation failed with expected SplitBrainProtectionException: " + expected.getMessage());
        }

        Hazelcast.shutdownAll();
    }
}
