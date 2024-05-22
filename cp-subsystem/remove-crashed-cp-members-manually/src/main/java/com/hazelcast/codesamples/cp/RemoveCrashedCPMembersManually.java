package com.hazelcast.codesamples.cp;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPGroup;
import com.hazelcast.cp.CPSubsystemManagementService;
import com.hazelcast.cp.IAtomicLong;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

/**
 * In this demo, we remove crashed CP members from the CP Subsystem manually
 *
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class RemoveCrashedCPMembersManually {

    private static final int CP_MEMBER_COUNT = 5;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Config config = new Config();
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        config.getCPSubsystemConfig().setCPMemberCount(CP_MEMBER_COUNT);
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz3 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz4 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz5 = Hazelcast.newHazelcastInstance(config);

        for (HazelcastInstance hz : Arrays.asList(hz1, hz2, hz3, hz4, hz5)) {
            hz.getCPSubsystem().getCPSubsystemManagementService().awaitUntilDiscoveryCompleted(1, TimeUnit.MINUTES);

            System.out.println(hz.getCluster().getLocalMember() + " initialized the CP subsystem with identity: "
                    + hz.getCPSubsystem().getLocalCPMember());
        }

        IAtomicLong counter = hz1.getCPSubsystem().getAtomicLong("counter");
        counter.incrementAndGet();

        UUID hz3CPMemberUid = hz3.getCPSubsystem().getLocalCPMember().getUuid();
        UUID hz4CPMemberUid = hz4.getCPSubsystem().getLocalCPMember().getUuid();
        UUID hz5CPMemberUid = hz5.getCPSubsystem().getLocalCPMember().getUuid();

        // 2 CP members crash...
        hz3.getLifecycleService().terminate();
        hz4.getLifecycleService().terminate();

        // Crashed CP members are removed.
        CPSubsystemManagementService cpSubsystemManagementService = hz1.getCPSubsystem().getCPSubsystemManagementService();
        cpSubsystemManagementService.removeCPMember(hz3CPMemberUid).toCompletableFuture().get();
        cpSubsystemManagementService.removeCPMember(hz4CPMemberUid).toCompletableFuture().get();

        CPGroup metadataGroup = cpSubsystemManagementService.getCPGroup(CPGroup.METADATA_CP_GROUP_NAME)
                .toCompletableFuture().get();
        assert metadataGroup.members().size() == CP_MEMBER_COUNT - 2;
        System.out.println("Metadata CP group has the following CP members: " + metadataGroup.members());

        // Whoops! Another CP member crashes...
        hz5.getLifecycleService().terminate();

        System.out.println("THe CP Subsystem is still available with 2 CP members running.");
        counter.incrementAndGet();

        cpSubsystemManagementService.removeCPMember(hz5CPMemberUid).toCompletableFuture().get();

        // Let's start new members and promote them to the CP role
        HazelcastInstance hz6 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz7 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz8 = Hazelcast.newHazelcastInstance(config);
        hz6.getCPSubsystem().getCPSubsystemManagementService().promoteToCPMember().toCompletableFuture().get();
        hz7.getCPSubsystem().getCPSubsystemManagementService().promoteToCPMember().toCompletableFuture().get();
        hz8.getCPSubsystem().getCPSubsystemManagementService().promoteToCPMember().toCompletableFuture().get();

        // Now all CP groups are recovered back to 5 CP members
        metadataGroup = cpSubsystemManagementService.getCPGroup(CPGroup.METADATA_CP_GROUP_NAME).toCompletableFuture().get();
        assert metadataGroup.members().size() == CP_MEMBER_COUNT;
        System.out.println("Metadata CP group has the following CP members: " + metadataGroup.members());

        for (HazelcastInstance hz : Arrays.asList(hz1, hz2, hz6, hz7, hz8)) {
            hz.shutdown();
        }
    }
}
