import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.SplitBrainProtectionConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.splitbrainprotection.SplitBrainProtectionException;

/**
 * The term "split brain protection" simply refers to the count of members in the cluster required for an operation to succeed.
 * It does NOT refer to an implementation of Paxos or Raft protocols as used in many NoSQL and distributed systems.
 * The mechanism it provides in Hazelcast protects the user in case the number of nodes in a cluster drops below the
 * specified one.
 *
 * Hazelcast Split brain protection is supported in the following data-structures:
 * - IMap
 * - TransactionalMap
 * - ICache
 * - IQueue
 * - TransactionalQueue
 * - ILock
 * - ISet
 * - TransactionalSet
 * - IList
 * - TransactionalList
 * - ISemaphore
 * - ICountDownLatch
 * - IAtomicLong
 * - IAtomicReference
 * - ReplicatedMap
 * - MultiMap
 * - IExecutorService
 * - DurableExecutorService
 * - IScheduledExecutorService
 * - Ringbuffer
 * - CardinalityEstimator
 */
public class ClusterSplitBrainProtection {

    private enum SplitBrainProtectionChoice {
        MEMBER_COUNT,
        PROBABILISTIC,
        RECENTLY_ACTIVE
    }
    private static final String NAME = "AT_LEAST_TWO_NODES";

    public static void main(String[] args) throws Exception {
        SplitBrainProtectionChoice choice = SplitBrainProtectionChoice.MEMBER_COUNT;
        if (args.length == 1) {
            choice = SplitBrainProtectionChoice.valueOf(args[0]);
        }
        SplitBrainProtectionConfig splitBrainProtectionConfig;

        switch (choice) {
            case PROBABILISTIC:
                splitBrainProtectionConfig = probabilisticSplitBrainProtectionConfig();
                break;
            case RECENTLY_ACTIVE:
                splitBrainProtectionConfig = recentlyActiveSplitBrainProtectionConfig();
                break;
            default:
                splitBrainProtectionConfig = memberCountSplitBrainProtectionConfig();
        }

        System.out.println("Configured split brain protection is " + splitBrainProtectionConfig);

        MapConfig mapConfig = new MapConfig();
        mapConfig.setName(NAME).setSplitBrainProtectionName(NAME);

        Config config = new Config();
        config.addMapConfig(mapConfig);
        config.addSplitBrainProtectionConfig(splitBrainProtectionConfig);

        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(config);

        IMap<String, String> map = instance1.getMap(NAME);

        // Split brain protection will succeed
        System.out.println("Split brain protection is satisfied, so the following put will throw no exception");
        map.put("key1", "we have the split brain protection");

        String value = map.get("key1");
        System.out.println("'key1' has the value '" + value + "'");

        // Split brain protection will fail
        System.out.println("Shutdown one instance, so there won't be enough members for split brain protection presence");
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

    private static SplitBrainProtectionConfig memberCountSplitBrainProtectionConfig() {
        SplitBrainProtectionConfig splitBrainProtectionConfig = new SplitBrainProtectionConfig();
        splitBrainProtectionConfig.setName(NAME).setEnabled(true).setMinimumClusterSize(2);
        return splitBrainProtectionConfig;
    }

    private static SplitBrainProtectionConfig recentlyActiveSplitBrainProtectionConfig() {
        SplitBrainProtectionConfig splitBrainProtectionConfig =
              SplitBrainProtectionConfig.newRecentlyActiveSplitBrainProtectionConfigBuilder(NAME, 2, 20000).build();
        return splitBrainProtectionConfig;
    }

    private static SplitBrainProtectionConfig probabilisticSplitBrainProtectionConfig() {
        SplitBrainProtectionConfig splitBrainProtectionConfig =
              SplitBrainProtectionConfig.newProbabilisticSplitBrainProtectionConfigBuilder(NAME, 2)
                .withAcceptableHeartbeatPauseMillis(60000)
                .withHeartbeatIntervalMillis(5000)
                .withSuspicionThreshold(10)
                .build();
        return splitBrainProtectionConfig;
    }
}
