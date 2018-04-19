import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.QuorumConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.quorum.QuorumException;

/**
 * The term "quorum" simply refers to the count of members in the cluster required for an operation to succeed.
 * It does NOT refer to an implementation of Paxos or Raft protocols as used in many NoSQL and distributed systems.
 * The mechanism it provides in Hazelcast protects the user in case the number of nodes in a cluster drops below the
 * specified one.
 *
 * Hazelcast Quorum is supported in the following data-structures:
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
public class ClusterQuorum {

    private enum QuorumChoice {
        MEMBER_COUNT,
        PROBABILISTIC,
        RECENTLY_ACTIVE
    }
    private static final String NAME = "AT_LEAST_TWO_NODES";

    public static void main(String[] args) throws Exception {
        QuorumChoice choice = QuorumChoice.MEMBER_COUNT;
        if (args.length == 1) {
            choice = QuorumChoice.valueOf(args[0]);
        }
        QuorumConfig quorumConfig;

        switch (choice) {
            case PROBABILISTIC:
                quorumConfig = probabilisticQuorumConfig();
                break;
            case RECENTLY_ACTIVE:
                quorumConfig = recentlyActiveQuorumConfig();
                break;
            default:
                quorumConfig = memberCountQuorumConfig();
        }

        System.out.println("Configured quorum is " + quorumConfig);

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
        // wait for a moment to detect that cluster fell apart
        Thread.sleep(1000);

        System.out.println("The following put operation will fail");
        try {
            map.put("key2", "will not succeed");
        } catch (QuorumException expected) {
            System.out.println("Put operation failed with expected QuorumException: " + expected.getMessage());
        }

        Hazelcast.shutdownAll();
    }

    private static QuorumConfig memberCountQuorumConfig() {
        QuorumConfig quorumConfig = new QuorumConfig();
        quorumConfig.setName(NAME).setEnabled(true).setSize(2);
        return quorumConfig;
    }

    private static QuorumConfig recentlyActiveQuorumConfig() {
        QuorumConfig quorumConfig = QuorumConfig.newRecentlyActiveQuorumConfigBuilder(NAME, 2, 20000).build();
        return quorumConfig;
    }

    private static QuorumConfig probabilisticQuorumConfig() {
        QuorumConfig quorumConfig = QuorumConfig.newProbabilisticQuorumConfigBuilder(NAME, 2)
                .withAcceptableHeartbeatPauseMillis(60000)
                .withHeartbeatIntervalMillis(5000)
                .withSuspicionThreshold(10)
                .build();
        return quorumConfig;
    }
}
