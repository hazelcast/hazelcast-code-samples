package com.hazelcast.codesamples.cp.fencedlock;

import com.hazelcast.config.Config;
import com.hazelcast.config.cp.CPSubsystemConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

/**
 * This code sample demonstrates that a Hazelcast instance releases the lock
 * instances it holds during shutdown.
 *
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class AutoReleaseFencedLockOnShutdown {

    public static void main(String[] args) {
        Config config = new Config();
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        CPSubsystemConfig cpSubsystemConfig = config.getCPSubsystemConfig();
        cpSubsystemConfig.setCPMemberCount(3);
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz3 = Hazelcast.newHazelcastInstance(config);

        hz1.getCPSubsystem().getLock("my-lock").lock();
        hz1.shutdown();

        FencedLock lock = hz2.getCPSubsystem().getLock("my-lock");
        assert !lock.isLocked();

        // always destroy CP Subsystem data structures otherwise it can lead to a memory leak
        lock.destroy();

        hz1.getLifecycleService().terminate();
        hz2.getLifecycleService().terminate();
        hz3.getLifecycleService().terminate();
    }
}
