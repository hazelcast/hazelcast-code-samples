package com.hazelcast.codesamples.cp;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPGroup;
import com.hazelcast.cp.CPSubsystemManagementService;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

/**
 * In this demo, we start the CP subsystem with a group size
 * different than the CP member count
 *
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class StartCPSubsystem2 {

    private static final int CP_MEMBER_COUNT = 5;
    private static final int GROUP_SIZE = 3;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Config config = new Config();
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        config.getCPSubsystemConfig().setCPMemberCount(CP_MEMBER_COUNT);
        config.getCPSubsystemConfig().setGroupSize(GROUP_SIZE);
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

        CPSubsystemManagementService cpSubsystemManagementService = hz1.getCPSubsystem().getCPSubsystemManagementService();
        CPGroup metadataGroup = cpSubsystemManagementService.getCPGroup(CPGroup.METADATA_CP_GROUP_NAME)
                .toCompletableFuture().get();
        assert metadataGroup.members().size() == GROUP_SIZE;
        System.out.println("Metadata CP group has the following CP members: " + metadataGroup.members());

        // Let's initiate the Default CP group

        hz1.getCPSubsystem().getAtomicLong("counter1").incrementAndGet();

        CPGroup defaultGroup = cpSubsystemManagementService.getCPGroup(CPGroup.DEFAULT_GROUP_NAME).toCompletableFuture().get();
        assert defaultGroup.members().size() == GROUP_SIZE;
        System.out.println("Default CP group has the following CP members: " + defaultGroup.members());

        // Let's create another CP group

        String customCPGroupName = "custom";
        hz1.getCPSubsystem().getAtomicLong("counter2@" + customCPGroupName).incrementAndGet();

        CPGroup customGroup = cpSubsystemManagementService.getCPGroup(customCPGroupName).toCompletableFuture().get();
        assert customGroup.members().size() == GROUP_SIZE;
        System.out.println(customCPGroupName + " CP group has the following CP members: " + customGroup.members());

        for (HazelcastInstance hz : Arrays.asList(hz1, hz2, hz3, hz4, hz5)) {
            hz.getLifecycleService().terminate();
        }
    }
}
