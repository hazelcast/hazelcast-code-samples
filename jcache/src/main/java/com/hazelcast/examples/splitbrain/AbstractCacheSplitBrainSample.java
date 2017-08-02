package com.hazelcast.examples.splitbrain;

import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.instance.Node;
import com.hazelcast.util.ExceptionUtil;

import java.io.FileNotFoundException;
import java.util.concurrent.CountDownLatch;

import static com.hazelcast.examples.helper.CommonUtils.assertClusterSizeEventually;
import static com.hazelcast.examples.helper.CommonUtils.assertOpenEventually;
import static com.hazelcast.examples.helper.CommonUtils.generateRandomString;
import static com.hazelcast.examples.helper.HazelcastUtils.getNode;

/**
 * Base class for jcache split-brain samples.
 */
public abstract class AbstractCacheSplitBrainSample {

    protected static final String BASE_CACHE_NAME = "my-cache";

    protected static Config newProgrammaticConfig() {
        Config config = new Config();
        config.setProperty("hazelcast.merge.first.run.delay.seconds", "5");
        config.setProperty("hazelcast.merge.next.run.delay.seconds", "3");
        config.getGroupConfig().setName(generateRandomString(10));
        return config;
    }

    protected static Config newDeclarativeConfig() {
        try {
            Config config = new XmlConfigBuilder("src/main/resources/hazelcast-splitbrain.xml").build();
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

    private static void closeConnectionBetween(HazelcastInstance h1, HazelcastInstance h2) {
        if (h1 == null || h2 == null) {
            return;
        }
        Node n1 = getNode(h1);
        Node n2 = getNode(h2);
        if (n1 != null && n2 != null) {
            n1.clusterService.suspectMember(n2.getLocalMember(), null, true);
            n2.clusterService.suspectMember(n1.getLocalMember(), null, true);
        }
    }
}
