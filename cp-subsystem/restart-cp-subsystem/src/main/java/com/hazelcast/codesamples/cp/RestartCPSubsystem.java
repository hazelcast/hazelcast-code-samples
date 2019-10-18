package com.hazelcast.codesamples.cp;

import com.hazelcast.config.Config;
import com.hazelcast.config.cp.CPSubsystemConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPMember;
import com.hazelcast.cp.CPSubsystemManagementService;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * In this demo, we restart the CP Subsystem when its majority crashes
 */
public class RestartCPSubsystem {

    private static final int CP_MEMBER_COUNT = 3;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Config config = new Config();
        CPSubsystemConfig cpSubsystemConfig = config.getCPSubsystemConfig();
        cpSubsystemConfig.setCPMemberCount(CP_MEMBER_COUNT);
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz3 = Hazelcast.newHazelcastInstance(config);

        for (HazelcastInstance hz : Arrays.asList(hz1, hz2, hz3)) {
            hz.getCPSubsystem().getCPSubsystemManagementService().awaitUntilDiscoveryCompleted(1, TimeUnit.MINUTES);

            System.out.println(hz.getCluster().getLocalMember() + " initialized the CP subsystem with identity: "
                    + hz.getCPSubsystem().getLocalCPMember());
        }

        // 2 CP member crashes and the CP Subsystem loses its availability :(
        hz2.getLifecycleService().terminate();
        hz3.getLifecycleService().terminate();

        // The only option left is to restart the CP Subsystem.
        // To do this, we need to make sure that there are 3 members in the Hazelcast cluster
        HazelcastInstance hz4 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz5 = Hazelcast.newHazelcastInstance(config);

        CPSubsystemManagementService cpSubsystemManagementService = hz1.getCPSubsystem().getCPSubsystemManagementService();
        cpSubsystemManagementService.restart().toCompletableFuture().get();

        for (HazelcastInstance hz : Arrays.asList(hz4, hz5)) {
            while (hz.getCPSubsystem().getLocalCPMember() == null) {
                Thread.sleep(1000);
            }

            System.out.println(hz.getCluster().getLocalMember() + " initialized the CP subsystem with identity: "
                    + hz.getCPSubsystem().getLocalCPMember());
        }

        // The CP subsystem is formed by the new cluster members
        Collection<CPMember> cpMembers = cpSubsystemManagementService.getCPMembers().toCompletableFuture().get();
        assert cpMembers.size() == 3;
        assert cpMembers.contains(hz1.getCPSubsystem().getLocalCPMember());
        assert cpMembers.contains(hz4.getCPSubsystem().getLocalCPMember());
        assert cpMembers.contains(hz5.getCPSubsystem().getLocalCPMember());

        for (HazelcastInstance hz : Arrays.asList(hz1, hz4, hz5)) {
            hz.getLifecycleService().terminate();
        }
    }
}
