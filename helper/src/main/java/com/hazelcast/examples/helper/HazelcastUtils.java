package com.hazelcast.examples.helper;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.Partition;
import com.hazelcast.core.PartitionService;
import com.hazelcast.instance.HazelcastInstanceImpl;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.instance.Node;
import com.hazelcast.internal.partition.InternalPartitionService;

import java.lang.reflect.Field;

import static com.hazelcast.examples.helper.CommonUtils.generateRandomString;

/**
 * Utils class for Hazelcast specific methods.
 */
public final class HazelcastUtils {

    private static final Field ORIGINAL_FIELD;

    static {
        try {
            ORIGINAL_FIELD = HazelcastInstanceProxy.class.getDeclaredField("original");
            ORIGINAL_FIELD.setAccessible(true);
        } catch (Throwable t) {
            throw new IllegalStateException("Unable to get `original` field in `HazelcastInstanceProxy`!", t);
        }
    }

    private HazelcastUtils() {
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

    public static Node getNode(HazelcastInstance hz) {
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
