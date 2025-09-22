package com.hazelcast.samples.testing.samples.junit5;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.hazelcast.test.HazelcastTestSupport.assertClusterSizeEventually;
import static com.hazelcast.test.HazelcastTestSupport.assertEqualsEventually;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MyClusterFailureTest {

    private HazelcastInstance client;
    private HazelcastInstance member1;
    private HazelcastInstance member2;
    private MembershipListener mockListener;

    private static Config getConfig(String v) {
        MemberAttributeConfig mAttr = new MemberAttributeConfig();
        mAttr.setAttribute("m", v);
        Config config = new Config();
        config.setMemberAttributeConfig(mAttr);
        return config;
    }

    @BeforeEach
    void setupCluster() {
        TestHazelcastFactory factory = new TestHazelcastFactory(2);
        member1 = factory.newHazelcastInstance(getConfig("1"));
        member2 = factory.newHazelcastInstance(getConfig("2"));

        ClientConfig clientConfig = new ClientConfig();
        mockListener = mock(MembershipListener.class);
        ListenerConfig listenerConfig = new ListenerConfig(mockListener);
        listenerConfig.setImplementation(mockListener);
        clientConfig.addListenerConfig(listenerConfig);
        client = factory.newHazelcastClient(clientConfig);
    }

    @AfterEach
    void tearDownCluster() {
        client.shutdown();
        if (member1 != null) {
            member1.shutdown();
        }
        if (member2 != null) {
            member2.shutdown();
        }
    }

    @Test
    void testClusterFailure() {
        assertClusterSizeEventually(2, client);
        member1.getMap("testMap").put("key1", "value1");
        assertEqualsEventually(() -> client.getMap("testMap").get("key1"), "value1");
        member1.shutdown();
        assertClusterSizeEventually(1, client);
        member1 = null;
        assertEqualsEventually(() -> client.getMap("testMap").get("key1"), "value1");
        ArgumentCaptor<MembershipEvent> membershipCaptor = ArgumentCaptor.forClass(MembershipEvent.class);
        verify(mockListener).memberRemoved(membershipCaptor.capture());
        MembershipEvent membershipEvent = membershipCaptor.getValue();
        assertEqualsEventually(() -> membershipEvent.getMember().getAttribute("m"), "1");
    }
}
