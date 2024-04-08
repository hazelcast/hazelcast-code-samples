package com.hazelcast.codesamples.cp;

import com.hazelcast.config.Config;
import com.hazelcast.config.cp.CPSubsystemConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPGroup;
import com.hazelcast.cp.CPSubsystemManagementService;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

/**
 * In this demo, we remove crashed CP members from the CP Subsystem automatically
 *
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class RemoveCrashedCPMemberAutomatically {

    private static final int CP_MEMBER_COUNT = 5;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Config config = new Config();
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        CPSubsystemConfig cpSubsystemConfig = config.getCPSubsystemConfig();
        cpSubsystemConfig.setCPMemberCount(CP_MEMBER_COUNT);
        cpSubsystemConfig.setSessionHeartbeatIntervalSeconds(1);
        cpSubsystemConfig.setSessionTimeToLiveSeconds(5);
        cpSubsystemConfig.setMissingCPMemberAutoRemovalSeconds(10);
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

        // We add a new CP member to the cluster.
        HazelcastInstance hz6 = Hazelcast.newHazelcastInstance(config);
        hz6.getCPSubsystem().getCPSubsystemManagementService().promoteToCPMember().toCompletableFuture().get();

        // A CP member crashes...
        hz5.getLifecycleService().terminate();

        CPSubsystemManagementService cpSubsystemManagementService = hz1.getCPSubsystem().getCPSubsystemManagementService();

        // The crashed CP member will be automatically removed and substituted by the new CP member.
        while (true) {
            CPGroup metadataGroup = cpSubsystemManagementService.getCPGroup(CPGroup.METADATA_CP_GROUP_NAME)
                    .toCompletableFuture().get();
            if (metadataGroup.members().size() == CP_MEMBER_COUNT
                    && metadataGroup.members().contains(hz6.getCPSubsystem().getLocalCPMember())) {
                System.out.println("The promoted member has been added to the Metadata CP group member list: "
                        + metadataGroup.members());
                break;
            }

            Thread.sleep(1000);
        }

        for (HazelcastInstance hz : Arrays.asList(hz1, hz2, hz3, hz4, hz6)) {
            hz.shutdown();
        }
    }
}
