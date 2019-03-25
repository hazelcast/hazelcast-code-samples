package com.hazelcast.codesamples.cp.fencedlock;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;

import java.util.concurrent.TimeUnit;

/**
 * This code sample demonstrates basic usage of the locking methods available
 * in the {@link FencedLock} interface
 */
public class FencedLockBasicUsage {

    public static void main(String[] args) {
        Config config = new Config();
        config.getCPSubsystemConfig().setCPMemberCount(3);
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz3 = Hazelcast.newHazelcastInstance(config);

        FencedLock hz1Lock = hz1.getCPSubsystem().getLock("my-lock");
        hz1Lock.lock();

        FencedLock hz2Lock = hz2.getCPSubsystem().getLock("my-lock");
        boolean lockedByHz2 = hz2Lock.tryLock(10, TimeUnit.SECONDS);
        assert !lockedByHz2;

        hz1Lock.unlock();

        FencedLock hz3Lock = hz3.getCPSubsystem().getLock("my-lock");
        boolean lockedByHz3 = hz3Lock.tryLock();
        assert lockedByHz3;

        hz1.getLifecycleService().terminate();
        hz2.getLifecycleService().terminate();
        hz3.getLifecycleService().terminate();
    }
}
