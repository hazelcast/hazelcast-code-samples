/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.examples.helper;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.Partition;
import com.hazelcast.core.PartitionService;
import com.hazelcast.instance.HazelcastInstanceImpl;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.instance.Node;
import com.hazelcast.internal.cluster.ClusterService;
import com.hazelcast.internal.partition.InternalPartitionService;
import com.hazelcast.internal.partition.impl.InternalPartitionServiceImpl;
import com.hazelcast.internal.partition.impl.PartitionServiceState;
import com.hazelcast.nio.Address;

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
        if (impl != null) {
            return impl.node;
        }
        throw new IllegalArgumentException("Could not get Node from HazelcastInstance " + hz);
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

    public static PartitionServiceState getPartitionServiceState(HazelcastInstance instance) {
        return getPartitionServiceState(getNode(instance));
    }

    public static PartitionServiceState getPartitionServiceState(Node node) {
        if (node == null) {
            return PartitionServiceState.SAFE;
        }
        InternalPartitionServiceImpl partitionService = (InternalPartitionServiceImpl) node.getPartitionService();
        return partitionService.getPartitionReplicaStateChecker().getPartitionServiceState();
    }

    public static Address getAddress(HazelcastInstance hz) {
        return getClusterService(hz).getThisAddress();
    }

    public static ClusterService getClusterService(HazelcastInstance hz) {
        return getNode(hz).clusterService;
    }

    public static void closeConnectionBetween(HazelcastInstance h1, HazelcastInstance h2) {
        if (h1 == null || h2 == null) {
            return;
        }
        Node n1 = getNode(h1);
        Node n2 = getNode(h2);
        suspectMember(n1, n2);
        suspectMember(n2, n1);
    }

    public static void suspectMember(Node suspectingNode, Node suspectedNode, String reason) {
        if (suspectingNode != null && suspectedNode != null) {
            Member suspectedMember = suspectingNode.getClusterService().getMember(suspectedNode.getLocalMember().getAddress());
            if (suspectedMember != null) {
                suspectingNode.clusterService.suspectMember(suspectedMember, reason, true);
            }
        }
    }

    public static void suspectMember(HazelcastInstance source, HazelcastInstance target) {
        suspectMember(getNode(source), getNode(target));
    }

    public static void suspectMember(Node suspectingNode, Node suspectedNode) {
        suspectMember(suspectingNode, suspectedNode, null);
    }
}
