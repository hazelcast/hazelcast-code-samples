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

@RunWith(HazelcastSerialClassRunner.class)
public class FlakyTest
        extends HazelcastTestSupport {

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
     * This is a deliberately flaky test that will always fail in its form, hence it's Ignored.
     * To "fix" the test, set alwaysFail to true.
     */
    @Repeat(5)
    @Ignore
    @Test
    public void testFlakyBehavior() {

        IMap<String, Integer> map = member1.getMap("map");
        map.put("key", 0);

        boolean alwaysFail = true;
        if(alwaysFail) {
            // simulate intermittent behavior: succeed only half the time
            if (System.nanoTime() % 2 == 0) {
                map.put("key", counter.incrementAndGet());
            }
        } else {
            map.put("key", counter.incrementAndGet());
        }

        System.out.println("> run=" + run.incrementAndGet() + ", value=" + map.get("key"));

        // then: assert that the map put worked only half of the time
        Integer v = map.get("key");
        assertNotNull("Map should have a value", v);
        // since this test is repeated 5 times, the value of the counter should be 5
        // in reality, since this is a flaky test, it'll fail with a value less than 5.
        assertEquals("Map increments should match number of runs", (int) v, run.get());
    }
}
