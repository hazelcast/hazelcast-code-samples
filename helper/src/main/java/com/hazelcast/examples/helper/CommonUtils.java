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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.internal.partition.impl.PartitionServiceState;
import com.hazelcast.nio.Address;
import com.hazelcast.nio.ConnectionManager;
import com.hazelcast.util.ExceptionUtil;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static com.hazelcast.examples.helper.HazelcastUtils.getAddress;
import static com.hazelcast.examples.helper.HazelcastUtils.getNode;
import static com.hazelcast.examples.helper.HazelcastUtils.getPartitionServiceState;
import static com.hazelcast.util.EmptyStatement.ignore;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Utils class for common methods.
 */
public final class CommonUtils {

    private static final Field ORIGINAL_FIELD;
    private static final int ASSERT_EVENTUALLY_TIMEOUT = 120;

    static {
        try {
            ORIGINAL_FIELD = HazelcastInstanceProxy.class.getDeclaredField("original");
            ORIGINAL_FIELD.setAccessible(true);
        } catch (Throwable t) {
            throw new IllegalStateException("Unable to get `original` field in `HazelcastInstanceProxy`!", t);
        }
    }

    private CommonUtils() {
    }

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char character = (char) (random.nextInt(26) + 'a');
            sb.append(character);
        }
        return sb.toString();
    }

    public static void assertTrue(String message, boolean condition) {
        if (!condition) {
            if (message == null) {
                throw new AssertionError();
            } else {
                throw new AssertionError(message);
            }
        }
    }

    public static void assertEquals(Object expected, Object actual) {
        assertEquals(null, expected, actual);
    }

    public static void assertEquals(String message, Object expected, Object actual) {
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

    public static void sleepSeconds(int seconds) {
        try {
            SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static boolean sleepMillis(int millis) {
        try {
            MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    public static void sleepAtLeastMillis(long sleepFor) {
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

    public static void assertClusterSize(int expectedSize, HazelcastInstance instance) {
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

    public static void assertClusterSizeEventually(final int expectedSize, final HazelcastInstance instance) {
        assertTrueEventually(new Runnable() {
            @Override
            public void run() {
                assertClusterSize(expectedSize, instance);
            }
        }, ASSERT_EVENTUALLY_TIMEOUT);
    }

    public static void assertOpenEventually(CountDownLatch latch) {
        assertOpenEventually(null, latch);
    }

    public static void assertOpenEventually(String message, CountDownLatch latch) {
        try {
            boolean completed = latch.await(ASSERT_EVENTUALLY_TIMEOUT, SECONDS);
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

    public static void waitAllForSafeState(Collection<HazelcastInstance> instances) {
        waitAllForSafeState(instances, 120);
    }

    public static void waitAllForSafeState(final Collection<HazelcastInstance> instances, int timeoutInSeconds) {
        assertTrueEventually(new Runnable() {
            public void run() {
                assertAllInSafeState(instances);
            }
        }, timeoutInSeconds);
    }

    public static void waitAllForSafeState(final HazelcastInstance[] nodes, int timeoutInSeconds) {
        assertTrueEventually(new Runnable() {
            public void run() {
                assertAllInSafeState(asList(nodes));
            }
        }, timeoutInSeconds);
    }

    public static void waitAllForSafeState(HazelcastInstance... nodes) {
        if (nodes.length == 0) {
            throw new IllegalArgumentException("waitAllForSafeState(HazelcastInstance... nodes) cannot be called with"
                    + " an empty array. It's too easy to mistake it for the old argument-less waitAllForSafeState()."
                    + " The old version was removed as it was implemented via Hazelcast.getAllHazelcastInstances()"
                    + " which uses a static map internally and it's causing issues when tests are running in parallel.");
        }
        waitAllForSafeState(asList(nodes));
    }

    public static void assertAllInSafeState(Collection<HazelcastInstance> nodes) {
        Map<Address, PartitionServiceState> nonSafeStates = new HashMap<Address, PartitionServiceState>();
        for (HazelcastInstance node : nodes) {
            if (node == null) {
                continue;
            }
            final PartitionServiceState state = getPartitionServiceState(node);
            if (state != PartitionServiceState.SAFE) {
                nonSafeStates.put(getAddress(node), state);
            }
        }

        assertTrue("Instances not in safe state! " + nonSafeStates, nonSafeStates.isEmpty());
    }

   public static void assertTrueEventually(Runnable task, long timeoutSeconds) {
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

    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
            ignore(ignored);
        }
    }
}
