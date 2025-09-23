package com.hazelcast.samples.testing.samples.junit4;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(HazelcastParallelClassRunner.class)
public class MyClusterTest
        extends HazelcastTestSupport {

    // Atomic timestamps to verify overlapping execution
    private static final AtomicLong test1Start = new AtomicLong();
    private static final AtomicLong test1End = new AtomicLong();
    private static final AtomicLong test2Start = new AtomicLong();
    private static final AtomicLong test2End = new AtomicLong();
    private HazelcastInstance[] instances;

    @AfterClass
    public static void verifyParallelExecution() {
        long start1 = test1Start.get();
        long end1 = test1End.get();
        long start2 = test2Start.get();
        long end2 = test2End.get();

        // Verify each test ran (timestamps set)
        assertTrue("Test1 did not record timing", start1 > 0 && end1 > start1);
        assertTrue("Test2 did not record timing", start2 > 0 && end2 > start2);

        // Verify intervals overlapped

        assertThat(start1).withFailMessage("Expected overlap but got [start1=%s, end1=%s, start2=%s, end2=%s]", start1, end1, start2, end2).isLessThan(end2);

        assertThat(start2).withFailMessage("Expected overlap but got [start1=%s, end1=%s, start2=%s, end2=%s]", start1, end1, start2, end2).isLessThan(end1);
    }

    @Before
    public void setUp() {
        Config config = new Config();
        config.setClusterName(randomName());
        // given: a 2-node in-process cluster
        instances = createHazelcastInstances(config, 2);
    }

    @After
    public void tearDown() {
        for (HazelcastInstance instance : instances) {
            instance.shutdown();
        }
    }

    @Test
    public void testMapPutAndGetAcrossCluster() {
        test1Start.set(System.currentTimeMillis());
        // simulate workload
        sleepMillis(100);

        // when: put an entry on member1
        IMap<String, String> mapOnMember1 = instances[0].getMap("testMap");
        mapOnMember1.put("hello", "world");

        // then: cluster forms and the entry is visible on member2
        assertClusterSizeEventually(2, instances[0]);
        IMap<String, String> mapOnMember2 = instances[1].getMap("testMap");
        assertEquals("world", mapOnMember2.get("hello"));

        test1End.set(System.currentTimeMillis());
    }

    @Test
    public void testMapRemoveAcrossCluster()
            throws Exception {
        test2Start.set(System.currentTimeMillis());
        // simulate workload
        Thread.sleep(100);

        // when: put and then remove an entry on member1
        IMap<String, String> mapOnMember1 = instances[0].getMap("testMap");
        mapOnMember1.put("tempKey", "tempValue");
        mapOnMember1.remove("tempKey");

        // then: cluster forms and the entry is removed on member2
        assertClusterSizeEventually(2, instances[0]);
        IMap<String, String> mapOnMember2 = instances[1].getMap("testMap");
        assertTrueEventually(() -> assertNull(mapOnMember2.get("tempKey")));

        test2End.set(System.currentTimeMillis());
    }
}
