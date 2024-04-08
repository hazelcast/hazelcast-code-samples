package com.hazelcast.codesamples.cp.fencedlock;

import com.hazelcast.config.Config;
import com.hazelcast.config.cp.FencedLockConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;
import com.hazelcast.cp.lock.exception.LockAcquireLimitReachedException;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

/**
 * This code sample demonstrates how a FencedLock can be used
 * as a non-reentrant mutex.
 *
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class NonReentrantMutex {

    public static void main(String[] args) {
        FencedLockConfig lockConfig = new FencedLockConfig("my-lock");
        lockConfig.disableReentrancy();
        Config config = new Config();
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        config.getCPSubsystemConfig().setCPMemberCount(3);
        config.getCPSubsystemConfig().addLockConfig(lockConfig);
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz3 = Hazelcast.newHazelcastInstance(config);

        FencedLock lock = hz1.getCPSubsystem().getLock("my-lock");
        lock.lock();

        try {
            lock.lock();
            assert false;
        } catch (LockAcquireLimitReachedException expected) {
            System.out.println("Cannot acquire the lock twice!");
        }

        // always destroy CP Subsystem data structures otherwise it can lead to a memory leak
        lock.destroy();

        hz1.getLifecycleService().terminate();
        hz2.getLifecycleService().terminate();
        hz3.getLifecycleService().terminate();
    }
}
