package com.hazelcast.examples.splitbrain;

import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.Partition;
import com.hazelcast.core.PartitionService;
import com.hazelcast.instance.HazelcastInstanceImpl;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.instance.Node;
import com.hazelcast.internal.partition.InternalPartitionService;
import com.hazelcast.nio.ConnectionManager;
import com.hazelcast.util.ExceptionUtil;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Base class for jcache split-brain samples.
 */
public abstract class AbstractCacheSplitBrainSample {

    protected static final String BASE_CACHE_NAME = "my-cache";

    private static final int ASSERT_EVENTUALLY_TIMEOUT = 120;
    private static final Field ORIGINAL_FIELD;

    static {
        try {
            ORIGINAL_FIELD = HazelcastInstanceProxy.class.getDeclaredField("original");
            ORIGINAL_FIELD.setAccessible(true);
        } catch (Throwable t) {
            throw new IllegalStateException("Unable to get `original` field in `HazelcastInstanceProxy`!", t);
        }
    }

    protected static void assertTrue(String message, boolean condition) {
        if (!condition) {
            if (message == null) {
                throw new AssertionError();
            } else {
                throw new AssertionError(message);
            }
        }
    }

    protected static void assertEquals(Object expected, Object actual) {
        assertEquals(null, expected, actual);
    }

    protected static void assertEquals(String message, Object expected, Object actual) {
        if (!equalsRegardingNull(expected, actual)) {
            failNotEquals(message, expected, actual);
        }
    }

    private static boolean equalsRegardingNull(Object expected, Object actual) {
        if (expected == null) {
            return actual == null;
        }
        return isEquals(expected, actual);
    }

    private static boolean isEquals(Object expected, Object actual) {
        return expected.equals(actual);
    }

    private static void failNotEquals(String message, Object expected, Object actual) {
        throw new AssertionError(format(message, expected, actual));
    }

    private static String format(String message, Object expected, Object actual) {
        String formatted = "";
        if (message != null && !message.equals("")) {
            formatted = message + " ";
        }
        String expectedString = String.valueOf(expected);
        String actualString = String.valueOf(actual);
        if (expectedString.equals(actualString)) {
            return formatted + "expected: "
                    + formatClassAndValue(expected, expectedString)
                    + " but was: " + formatClassAndValue(actual, actualString);
        } else {
            return formatted + "expected:<" + expectedString + "> but was:<" + actualString + ">";
        }
    }

    private static String formatClassAndValue(Object value, String valueString) {
        String className = value == null ? "null" : value.getClass().getName();
        return className + "<" + valueString + ">";
    }

    protected static void sleepMillis(int millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected static void sleepAtLeastMillis(long sleepFor) {
        boolean interrupted = false;
        try {
            long remainingNanos = MILLISECONDS.toNanos(sleepFor);
            long sleepUntil = System.nanoTime() + remainingNanos;
            while (remainingNanos > 0) {
                try {
                    NANOSECONDS.sleep(remainingNanos);
                } catch (InterruptedException e) {
                    interrupted = true;
                } finally {
                    remainingNanos = sleepUntil - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    protected static void assertClusterSize(int expectedSize, HazelcastInstance instance) {
        int clusterSize = instance.getCluster().getMembers().size();
        if (expectedSize != clusterSize) {
            ConnectionManager connectionManager = getNode(instance).getConnectionManager();
            int activeConnectionCount = connectionManager.getActiveConnectionCount();
            throw new AssertionError(String.format("Cluster size is not correct. Expected: %d Actual: %d %s",
                    expectedSize,
                    clusterSize,
                    "ActiveConnectionCount: " + activeConnectionCount));
        }
    }

    protected static void assertClusterSizeEventually(final int expectedSize, final HazelcastInstance instance) {
        assertTrueEventually(new Runnable() {
            @Override
            public void run() {
                assertClusterSize(expectedSize, instance);
            }
        }, ASSERT_EVENTUALLY_TIMEOUT);
    }

    protected static void assertOpenEventually(CountDownLatch latch) {
        assertOpenEventually(null, latch);
    }

    protected static void assertOpenEventually(String message, CountDownLatch latch) {
        try {
            boolean completed = latch.await(ASSERT_EVENTUALLY_TIMEOUT, TimeUnit.SECONDS);
            if (message == null) {
                assertTrue(String.format("CountDownLatch failed to complete within %d seconds , count left: %d",
                        ASSERT_EVENTUALLY_TIMEOUT,
                        latch.getCount()),
                        completed);
            } else {
                assertTrue(String.format("%s, failed to complete within %d seconds , count left: %d",
                        message,
                        ASSERT_EVENTUALLY_TIMEOUT,
                        latch.getCount()),
                        completed);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void assertTrueEventually(Runnable task, long timeoutSeconds) {
        try {
            AssertionError error = null;
            // we are going to check 5 times a second
            long iterations = timeoutSeconds * 5;
            int sleepMillis = 200;
            for (int i = 0; i < iterations; i++) {
                try {
                    try {
                        task.run();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return;
                } catch (AssertionError e) {
                    error = e;
                }
                sleepMillis(sleepMillis);
            }
            throw error;
        } catch (Throwable t) {
            throw ExceptionUtil.rethrow(t);
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

    private static void closeConnectionBetween(HazelcastInstance h1, HazelcastInstance h2) {
        if (h1 == null || h2 == null) {
            return;
        }
        Node n1 = getNode(h1);
        Node n2 = getNode(h2);
        if (n1 != null && n2 != null) {
            n1.clusterService.removeAddress(n2.address, null);
            n2.clusterService.removeAddress(n1.address, null);
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

    protected static String generateKeyOwnedBy(HazelcastInstance instance) {
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

    protected static Config newProgrammaticConfig() {
        Config config = new Config();
        config.setProperty("hazelcast.merge.first.run.delay.seconds", "5");
        config.setProperty("hazelcast.merge.next.run.delay.seconds", "3");
        config.getGroupConfig().setName(generateRandomString(10));
        return config;
    }

    protected static Config newDeclarativeConfig() {
        try {
            Config config =
                    new XmlConfigBuilder("jcache/src/main/resources/hazelcast-splitbrain.xml").build();
            config.setProperty("hazelcast.merge.first.run.delay.seconds", "5");
            config.setProperty("hazelcast.merge.next.run.delay.seconds", "3");
            config.getGroupConfig().setName(generateRandomString(10));
            return config;
        } catch (FileNotFoundException e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    protected static CacheConfig newCacheConfig(String cacheName, String mergePolicy) {
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setName(cacheName);
        cacheConfig.setMergePolicy(mergePolicy);
        return cacheConfig;
    }

    private static final class SampleLifeCycleListener implements LifecycleListener {

        private final CountDownLatch latch;

        private SampleLifeCycleListener(int countdown) {
            latch = new CountDownLatch(countdown);
        }

        @Override
        public void stateChanged(LifecycleEvent event) {
            if (event.getState() == LifecycleEvent.LifecycleState.MERGED) {
                latch.countDown();
            }
        }
    }

    private static final class SampleMemberShipListener implements MembershipListener {

        private final CountDownLatch latch;

        private SampleMemberShipListener(int countdown) {
            latch = new CountDownLatch(countdown);
        }

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {

        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            latch.countDown();
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        }
    }

    protected static CountDownLatch simulateSplitBrain(HazelcastInstance h1, HazelcastInstance h2) {
        SampleMemberShipListener memberShipListener = new SampleMemberShipListener(1);
        h2.getCluster().addMembershipListener(memberShipListener);
        SampleLifeCycleListener lifeCycleListener = new SampleLifeCycleListener(1);
        h2.getLifecycleService().addLifecycleListener(lifeCycleListener);

        closeConnectionBetween(h1, h2);

        assertOpenEventually(memberShipListener.latch);
        assertClusterSizeEventually(1, h1);
        assertClusterSizeEventually(1, h2);

        return lifeCycleListener.latch;
    }
}
