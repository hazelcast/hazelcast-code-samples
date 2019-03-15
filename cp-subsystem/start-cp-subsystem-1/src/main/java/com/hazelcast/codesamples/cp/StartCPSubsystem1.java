package com.hazelcast.codesamples.cp;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * In this demo, we start a cluster with both CP members and AP members.
 */
public class StartCPSubsystem1 {

    private static final int CP_MEMBER_COUNT = 3;

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        config.getCPSubsystemConfig().setCPMemberCount(CP_MEMBER_COUNT);
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz3 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz4 = Hazelcast.newHazelcastInstance(config);

        for (HazelcastInstance hz : Arrays.asList(hz1, hz2, hz3)) {
            hz.getCPSubsystem().getCPSubsystemManagementService().awaitUntilDiscoveryCompleted(1, TimeUnit.MINUTES);

            System.out.println(hz.getCluster().getLocalMember() + " initialized the CP subsystem with identity: "
                    + hz.getCPSubsystem().getLocalCPMember());
        }

        // We can access to the CP data structures from any Hazelcast member
        System.out.println(hz1.getCPSubsystem().getAtomicLong("counter").incrementAndGet());
        System.out.println(hz4.getCPSubsystem().getAtomicLong("counter").incrementAndGet());

        for (HazelcastInstance hz : Arrays.asList(hz1, hz2, hz3, hz4)) {
            hz.getLifecycleService().terminate();
        }
    }
}
