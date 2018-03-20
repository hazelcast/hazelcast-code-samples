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

package com.hazelcast.examples.helper.splitbrain;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import com.hazelcast.examples.helper.nio.FirewallingConnectionManager;
import com.hazelcast.examples.helper.nio.FirewallingNodeContext;
import com.hazelcast.instance.HazelcastInstanceFactory;
import com.hazelcast.instance.HazelcastInstanceImpl;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.instance.Node;
import com.hazelcast.instance.NodeState;
import com.hazelcast.spi.properties.GroupProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hazelcast.examples.helper.CommonUtils.assertClusterSizeEventually;
import static com.hazelcast.examples.helper.CommonUtils.assertOpenEventually;
import static com.hazelcast.examples.helper.CommonUtils.waitAllForSafeState;
import static com.hazelcast.examples.helper.HazelcastUtils.closeConnectionBetween;
import static com.hazelcast.examples.helper.HazelcastUtils.getNode;

/**
 * A support class for high-level split-brain tests.
 * <p>
 * Forms a cluster, creates a split-brain situation and then heals the cluster again.
 * <p>
 * Implementing tests are supposed to subclass this class and use its hooks to be notified about state transitions.
 * See {@link #onBeforeSplitBrainCreated(HazelcastInstance[])},
 * {@link #onAfterSplitBrainCreated(HazelcastInstance[], HazelcastInstance[])}
 * and {@link #onAfterSplitBrainHealed(HazelcastInstance[])}
 * <p>
 * The current implementation always isolates the first member of the cluster, but it should be simple to customize this
 * class to support mode advanced split-brain scenarios.
 */
@SuppressWarnings("WeakerAccess")
public abstract class SplitBrainTestSupport {

    // per default the second half should merge into the first half
    private static final int[] DEFAULT_BRAINS = new int[]{2, 1};
    private static final int DEFAULT_ITERATION_COUNT = 1;
    private static final AtomicInteger FACTORY_ID_GEN = new AtomicInteger();

    private static final SplitBrainAction BLOCK_COMMUNICATION = new SplitBrainAction() {
        @Override
        public void apply(HazelcastInstance h1, HazelcastInstance h2) {
            blockCommunicationBetween(h1, h2);
        }
    };

    private static final SplitBrainAction UNBLOCK_COMMUNICATION = new SplitBrainAction() {
        @Override
        public void apply(HazelcastInstance h1, HazelcastInstance h2) {
            unblockCommunicationBetween(h1, h2);
        }
    };

    private static final SplitBrainAction CLOSE_CONNECTION = new SplitBrainAction() {
        @Override
        public void apply(HazelcastInstance h1, HazelcastInstance h2) {
            closeConnectionBetween(h1, h2);
        }
    };

    private int[] brains;
    private HazelcastInstance[] instances;

    public SplitBrainTestSupport() {
        brains = brains();
        validateBrainsConfig(brains);
    }

    public void run() throws Exception {
        try {
            onBeforeSetup();

            Config config = config();
            int clusterSize = getClusterSize();
            instances = startInitialCluster(config, clusterSize);

            for (int i = 0; i < iterations(); i++) {
                doIteration();
            }
        } finally {
            for (HazelcastInstance hazelcastInstance : Hazelcast.getAllHazelcastInstances()) {
                hazelcastInstance.getLifecycleService().terminate();
            }
        }
    }

    /**
     * Override this method to execute initialization that may be required before instantiating the cluster. This is the
     * first method executed by {@code @Before SplitBrainTestSupport.setupInternals}.
     */
    protected void onBeforeSetup() {
    }

    /**
     * Override this method to create a custom brain sizes
     *
     * @return the default number of brains
     */
    protected int[] brains() {
        return DEFAULT_BRAINS;
    }

    /**
     * Override this method to create a custom Hazelcast configuration.
     *
     * @return the default Hazelcast configuration
     */
    protected Config config() {
        return new Config()
                .setProperty(GroupProperty.PARTITION_COUNT.getName(), "11")
                .setProperty(GroupProperty.PARTITION_OPERATION_THREAD_COUNT.getName(), "2")
                .setProperty(GroupProperty.GENERIC_OPERATION_THREAD_COUNT.getName(), "2")
                .setProperty(GroupProperty.EVENT_THREAD_COUNT.getName(), "1")
                .setProperty(GroupProperty.MERGE_FIRST_RUN_DELAY_SECONDS.getName(), "5")
                .setProperty(GroupProperty.MERGE_NEXT_RUN_DELAY_SECONDS.getName(), "5");
    }

    /**
     * Override this method to create the split-brain situation multiple-times.
     * <p>
     * The test will use the same members for all iterations.
     *
     * @return the default number of iterations
     */
    protected int iterations() {
        return DEFAULT_ITERATION_COUNT;
    }

    /**
     * Called when a cluster is fully formed. You can use this method for test initialization, data load, etc.
     *
     * @param instances all Hazelcast instances in your cluster
     */
    @SuppressWarnings({"RedundantThrows", "unused"})
    protected void onBeforeSplitBrainCreated(HazelcastInstance[] instances) throws Exception {
    }

    /**
     * Called just after a split brain situation was created
     */
    @SuppressWarnings({"RedundantThrows", "unused"})
    protected void onAfterSplitBrainCreated(HazelcastInstance[] firstBrain, HazelcastInstance[] secondBrain) throws Exception {
    }

    /**
     * Called just after the original cluster was healed again. This is likely the place for various asserts.
     *
     * @param instances all Hazelcast instances in your cluster
     */
    @SuppressWarnings({"RedundantThrows", "unused"})
    protected void onAfterSplitBrainHealed(HazelcastInstance[] instances) throws Exception {
    }

    /**
     * Indicates whether test should fail when cluster does not include all original members after communications are unblocked.
     * <p>
     * Override this method when it is expected that after communications are unblocked some members will not rejoin the cluster.
     * When overriding this method, it may be desirable to add some wait time to allow the split brain handler to execute.
     *
     * @return {@code true} if the test should fail when not all original members rejoin after split brain is
     * healed, otherwise {@code false}.
     */
    protected boolean shouldAssertAllNodesRejoined() {
        return true;
    }

    private void doIteration() throws Exception {
        onBeforeSplitBrainCreated(instances);

        createSplitBrain();
        Brains brains = getBrains();
        onAfterSplitBrainCreated(brains.getFirstHalf(), brains.getSecondHalf());

        healSplitBrain();
        onAfterSplitBrainHealed(instances);
    }

    protected HazelcastInstance[] startInitialCluster(Config config, int clusterSize) {
        HazelcastInstance[] hazelcastInstances = new HazelcastInstance[clusterSize];
        for (int i = 0; i < clusterSize; i++) {
            hazelcastInstances[i] = HazelcastInstanceFactory.newHazelcastInstance(config, createInstanceName(config),
                    new FirewallingNodeContext());
        }
        return hazelcastInstances;
    }

    public static String createInstanceName(Config config) {
        return "_hzInstance_" + FACTORY_ID_GEN.incrementAndGet() + "_" + config.getGroupConfig().getName();
    }

    private void validateBrainsConfig(int[] clusterTopology) {
        if (clusterTopology.length != 2) {
            throw new AssertionError("Only simple topologies with 2 brains are supported. Current setup: "
                    + Arrays.toString(clusterTopology));
        }
    }

    private int getClusterSize() {
        int clusterSize = 0;
        for (int brainSize : brains) {
            clusterSize += brainSize;
        }
        return clusterSize;
    }

    private void createSplitBrain() {
        blockCommunications();
        closeExistingConnections();
        assertSplitBrainCreated();
    }

    private void assertSplitBrainCreated() {
        int firstHalfSize = brains[0];
        for (int isolatedIndex = 0; isolatedIndex < firstHalfSize; isolatedIndex++) {
            HazelcastInstance isolatedInstance = instances[isolatedIndex];
            assertClusterSizeEventually(firstHalfSize, isolatedInstance);
        }
        for (int i = firstHalfSize; i < instances.length; i++) {
            HazelcastInstance currentInstance = instances[i];
            assertClusterSizeEventually(instances.length - firstHalfSize, currentInstance);
        }
    }

    private void closeExistingConnections() {
        applyOnBrains(CLOSE_CONNECTION);
    }

    private void blockCommunications() {
        applyOnBrains(BLOCK_COMMUNICATION);
    }

    private void healSplitBrain() {
        MergeBarrier mergeBarrier = new MergeBarrier(instances);
        try {
            unblockCommunication();
            if (shouldAssertAllNodesRejoined()) {
                for (HazelcastInstance hz : instances) {
                    assertClusterSizeEventually(instances.length, hz);
                }
            }
            waitAllForSafeState(instances);
        } finally {
            mergeBarrier.awaitNoMergeInProgressAndClose();
        }
    }

    private void unblockCommunication() {
        applyOnBrains(UNBLOCK_COMMUNICATION);
    }

    private static FirewallingConnectionManager getFireWalledConnectionManager(HazelcastInstance hz) {
        return (FirewallingConnectionManager) getNode(hz).getConnectionManager();
    }

    protected Brains getBrains() {
        int firstHalfSize = brains[0];
        int secondHalfSize = brains[1];
        HazelcastInstance[] firstHalf = new HazelcastInstance[firstHalfSize];
        HazelcastInstance[] secondHalf = new HazelcastInstance[secondHalfSize];
        for (int i = 0; i < instances.length; i++) {
            if (i < firstHalfSize) {
                firstHalf[i] = instances[i];
            } else {
                secondHalf[i - firstHalfSize] = instances[i];
            }
        }
        return new Brains(firstHalf, secondHalf);
    }

    private void applyOnBrains(SplitBrainAction action) {
        int firstHalfSize = brains[0];
        for (int isolatedIndex = 0; isolatedIndex < firstHalfSize; isolatedIndex++) {
            HazelcastInstance isolatedInstance = instances[isolatedIndex];
            // do not take into account instances which have been shutdown
            if (!isInstanceActive(isolatedInstance)) {
                continue;
            }
            for (int i = firstHalfSize; i < instances.length; i++) {
                HazelcastInstance currentInstance = instances[i];
                if (!isInstanceActive(currentInstance)) {
                    continue;
                }
                action.apply(isolatedInstance, currentInstance);
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isInstanceActive(HazelcastInstance instance) {
        if (instance instanceof HazelcastInstanceProxy) {
            try {
                return ((HazelcastInstanceProxy) instance).getOriginal() != null;
            } catch (HazelcastInstanceNotActiveException exception) {
                return false;
            }
        } else if (instance instanceof HazelcastInstanceImpl) {
            return getNode(instance).getState() == NodeState.ACTIVE;
        } else {
            throw new AssertionError("Unsupported HazelcastInstance type");
        }
    }

    public static void blockCommunicationBetween(HazelcastInstance h1, HazelcastInstance h2) {
        FirewallingConnectionManager cm1 = getFireWalledConnectionManager(h1);
        FirewallingConnectionManager cm2 = getFireWalledConnectionManager(h2);
        Node node1 = getNode(h1);
        Node node2 = getNode(h2);
        cm1.blockNewConnection(node2.getThisAddress());
        cm2.blockNewConnection(node1.getThisAddress());
        cm1.closeActiveConnection(node2.getThisAddress());
        cm2.closeActiveConnection(node1.getThisAddress());
    }

    public static void unblockCommunicationBetween(HazelcastInstance h1, HazelcastInstance h2) {
        FirewallingConnectionManager cm1 = getFireWalledConnectionManager(h1);
        FirewallingConnectionManager cm2 = getFireWalledConnectionManager(h2);
        Node node1 = getNode(h1);
        Node node2 = getNode(h2);
        cm1.unblock(node2.getThisAddress());
        cm2.unblock(node1.getThisAddress());
    }

    public static String toString(Collection collection) {
        StringBuilder sb = new StringBuilder("[");
        String delimiter = "";
        for (Object item : collection) {
            sb.append(delimiter).append(item);
            delimiter = ", ";
        }
        sb.append("]");
        return sb.toString();
    }

    private interface SplitBrainAction {
        void apply(HazelcastInstance h1, HazelcastInstance h2);
    }

    protected static final class Brains {

        private final HazelcastInstance[] firstHalf;
        private final HazelcastInstance[] secondHalf;

        private Brains(HazelcastInstance[] firstHalf, HazelcastInstance[] secondHalf) {
            this.firstHalf = firstHalf;
            this.secondHalf = secondHalf;
        }

        public HazelcastInstance[] getFirstHalf() {
            return firstHalf;
        }

        public HazelcastInstance[] getSecondHalf() {
            return secondHalf;
        }
    }

    protected static final class MergeLifecycleListener implements LifecycleListener {

        private final CountDownLatch latch;

        public MergeLifecycleListener(int mergingClusterSize) {
            latch = new CountDownLatch(mergingClusterSize);
        }

        @Override
        public void stateChanged(LifecycleEvent event) {
            if (event.getState() == LifecycleEvent.LifecycleState.MERGED) {
                latch.countDown();
            }
        }

        public void await() {
            assertOpenEventually(latch);
        }
    }
}
