package com.hazelcast.samples.testing.samples.junit4;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.Repeat;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Example of a deliberately flaky test, left {@link Ignore}d to avoid polluting builds.
 *
 * <p>Illustrates the use of Hazelcast’s JUnit 4 test support to
 * spin up members and control test execution.
 *
 * <p><strong>HazelcastTestSupport illustrated:</strong>
 * <ul>
 *   <li>{@link HazelcastTestSupport#createHazelcastInstance()} to create a member</li>
 * </ul>
 *
 * <p><strong>HazelcastSerialClassRunner illustrated:</strong>
 * <ul>
 *   <li>Ensures tests in this class are run serially rather than in parallel</li>
 * </ul>
 *
 * <p><strong>@Repeat annotation illustrated:</strong>
 * <ul>
 *   <li>{@link Repeat} to re-run a test method multiple times</li>
 * </ul>
 */
@RunWith(HazelcastSerialClassRunner.class)
public class FlakyTest extends HazelcastTestSupport {

    private static final AtomicInteger run = new AtomicInteger();
    private static final AtomicInteger counter = new AtomicInteger();
    private HazelcastInstance member1;

    @Before
    public void setUp() {
        member1 = createHazelcastInstance();
    }

    @After
    public void tearDown() {
        if (member1 != null) {
            member1.shutdown();
        }
    }

    /**
     * Demonstrates a test that fails intermittently by design.
     *
     * <p>The test is repeated 5 times with {@link Repeat}, but since
     * {@code alwaysFail} is {@code true}, the counter will not increment
     * reliably and the final assertion will fail.
     *
     * <p>To "fix" the test, set {@code alwaysFail} to {@code false}.
     */
    @Repeat(5)
    @Ignore
    @Test
    public void testFlakyBehavior() {
        IMap<String, Integer> map = member1.getMap("map");
        map.put("key", 0);

        boolean alwaysFail = true;
        if (alwaysFail) {
            // simulate intermittent behaviour: succeed only half the time
            if (System.nanoTime() % 2 == 0) {
                map.put("key", counter.incrementAndGet());
            }
        } else {
            map.put("key", counter.incrementAndGet());
        }

        System.out.println("> run=" + run.incrementAndGet() + ", value=" + map.get("key"));

        Integer v = map.get("key");
        assertNotNull("Map should have a value", v);
        // Since the test is repeated 5 times, we expect the counter to equal the run count.
        // In reality, because it's flaky, the assertion will fail.
        assertEquals("Map increments should match number of runs", (int) v, run.get());
    }
}
