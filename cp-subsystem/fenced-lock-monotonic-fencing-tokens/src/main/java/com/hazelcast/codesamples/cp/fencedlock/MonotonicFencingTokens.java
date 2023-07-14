package com.hazelcast.codesamples.cp.fencedlock;

import com.hazelcast.config.Config;
import com.hazelcast.config.cp.CPSubsystemConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;

/**
 * This code sample demonstrates how fencing tokens returned by the FencedLock
 * increases every time the lock switches from the available state to the held
 * state.
 */
public class MonotonicFencingTokens {

    public static void main(String[] args) {
        Config config = new Config();
        CPSubsystemConfig cpSubsystemConfig = config.getCPSubsystemConfig();
        cpSubsystemConfig.setCPMemberCount(3);
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance hz3 = Hazelcast.newHazelcastInstance(config);

        // The lock switches from the available state to the held state.
        FencedLock hz1Lock = hz1.getCPSubsystem().getLock("my-lock");
        hz1Lock.lock();
        long fence1 = hz1Lock.getFence();
        hz1Lock.unlock();

        // The lock switches from the available state to the held state.
        FencedLock hz2Lock = hz2.getCPSubsystem().getLock("my-lock");
        hz2Lock.lock();
        long fence2 = hz2Lock.getFence();

        assert fence2 > fence1;

        // The lock is already held by the second instance.
        // Making a reentrant lock acquire.
        hz2Lock.lock();
        long fence3 = hz2Lock.getFence();

        assert fence3 == fence2;

        hz2Lock.unlock();
        hz2Lock.unlock();

        // The lock switches from the available state to the held state.
        hz2Lock.lock();
        long fence4 = hz2Lock.getFence();
        hz2Lock.unlock();

        assert fence4 > fence3;

        // always destroy CP Subsystem data structures otherwise it can lead to a memory leak
        hz1Lock.destroy();

        hz1.getLifecycleService().terminate();
        hz2.getLifecycleService().terminate();
        hz3.getLifecycleService().terminate();
    }
}
