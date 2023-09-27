package com.hazelcast.codesamples.cp.fencedlock;

import com.hazelcast.config.Config;
import com.hazelcast.config.cp.CPSubsystemConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;

import java.util.concurrent.TimeUnit;

/**
 * This code sample demonstrates that a FencedLock is eventually released when
 * its holder does not commit CP session heartbeats.
 */
public class AutoReleaseFencedLockOnSessionClose {

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        CPSubsystemConfig cpSubsystemConfig = config.getCPSubsystemConfig();
        cpSubsystemConfig.setCPMemberCount(3);
        cpSubsystemConfig.setSessionHeartbeatIntervalSeconds(1);
        cpSubsystemConfig.setSessionTimeToLiveSeconds(10);
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz3 = Hazelcast.newHazelcastInstance(config);

        // The first Hazelcast member acquires the lock
        hz1.getCPSubsystem().getLock("my-lock").lock();

        // The first Hazelcast member crashes.
        // After some time, the lock will be auto-release because of missing CP session heartbeats...
        hz1.getLifecycleService().terminate();

        FencedLock lock = hz2.getCPSubsystem().getLock("my-lock");
        while (lock.isLocked()) {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            System.out.println("Waiting for auto-release of the lock...");
        }

        System.out.println("The lock is automatically released...");

        // always destroy CP Subsystem data structures otherwise it can lead to a memory leak
        lock.destroy();

        hz1.getLifecycleService().terminate();
        hz2.getLifecycleService().terminate();
        hz3.getLifecycleService().terminate();
    }
}
