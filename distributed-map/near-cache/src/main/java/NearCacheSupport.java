import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.Partition;
import com.hazelcast.core.PartitionService;
import com.hazelcast.instance.HazelcastInstanceImpl;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.instance.Node;
import com.hazelcast.internal.partition.InternalPartitionService;
import com.hazelcast.monitor.NearCacheStats;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.hazelcast.spi.properties.GroupProperty.CACHE_INVALIDATION_MESSAGE_BATCH_FREQUENCY_SECONDS;
import static java.lang.Integer.parseInt;

public abstract class NearCacheSupport {

    private static final int INVALIDATION_DELAY_SECONDS
            = 2 * parseInt(CACHE_INVALIDATION_MESSAGE_BATCH_FREQUENCY_SECONDS.getDefaultValue());

    private static final Field ORIGINAL_FIELD;

    static {
        try {
            ORIGINAL_FIELD = HazelcastInstanceProxy.class.getDeclaredField("original");
            ORIGINAL_FIELD.setAccessible(true);
        } catch (Throwable t) {
            throw new IllegalStateException("Unable to get `original` field in `HazelcastInstanceProxy`!", t);
        }
    }

    public static void printNearCacheStats(IMap<Integer, Article> map) {
        NearCacheStats stats = map.getLocalMapStats().getNearCacheStats();

        System.out.printf("The Near Cache contains %d entries.%n", stats.getOwnedEntryCount());
        System.out.printf("The first article instance was retrieved from the remote instance (Near Cache misses: %d).%n",
                stats.getMisses());
        System.out.printf(
                "The second and third article instance were retrieved from the local Near Cache (Near Cache hits: %d).%n",
                stats.getHits());
    }

    public static void printNearCacheStats(IMap<?, Article> map, String message) {
        NearCacheStats stats = map.getLocalMapStats().getNearCacheStats();
        System.out.printf("%s (%d entries, %d hits, %d misses)%n",
                message, stats.getOwnedEntryCount(), stats.getHits(), stats.getMisses());
    }

    public static void waitForNearCacheEntryCount(IMap<?, Article> map, int targetSize) {
        long ownedEntries;
        do {
            NearCacheStats stats = map.getLocalMapStats().getNearCacheStats();
            ownedEntries = stats.getOwnedEntryCount();
        } while (ownedEntries > targetSize);
    }

    public static void waitForInvalidationEvents() {
        try {
            TimeUnit.SECONDS.sleep(INVALIDATION_DELAY_SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public static String generateKeyOwnedBy(HazelcastInstance instance) {
        Cluster cluster = instance.getCluster();
        checkPartitionCountGreaterOrEqualMemberCount(instance);

        Member localMember = cluster.getLocalMember();
        PartitionService partitionService = instance.getPartitionService();
        while (true) {
            String id = generateRandomString(10);
            Partition partition = partitionService.getPartition(id);
            Member owner = partition.getOwner();
            if (localMember.equals(owner)) {
                return id;
            }
        }
    }

    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char character = (char) (random.nextInt(26) + 'a');
            sb.append(character);
        }
        return sb.toString();
    }

    private static void checkPartitionCountGreaterOrEqualMemberCount(HazelcastInstance instance) {
        Cluster cluster = instance.getCluster();
        int memberCount = cluster.getMembers().size();

        Node node = getNode(instance);

        InternalPartitionService internalPartitionService = node.getPartitionService();
        int partitionCount = internalPartitionService.getPartitionCount();

        if (partitionCount < memberCount) {
            throw new UnsupportedOperationException("Partition count should be equal or greater than member count!");
        }
    }

    private static Node getNode(HazelcastInstance hz) {
        HazelcastInstanceImpl impl = getHazelcastInstanceImpl(hz);
        return impl != null ? impl.node : null;
    }

    private static HazelcastInstanceImpl getHazelcastInstanceImpl(HazelcastInstance hz) {
        HazelcastInstanceImpl impl = null;
        if (hz instanceof HazelcastInstanceProxy) {
            try {
                impl = (HazelcastInstanceImpl) ORIGINAL_FIELD.get(hz);
            } catch (Throwable t) {
                throw new IllegalStateException("Unable to get value of `original` in `HazelcastInstanceProxy`!", t);
            }
        } else if (hz instanceof HazelcastInstanceImpl) {
            impl = (HazelcastInstanceImpl) hz;
        }
        return impl;
    }
}
