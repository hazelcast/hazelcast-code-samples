package com.hazelcast.codesamples.cp.fencedlock;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;

import java.util.Date;
import java.util.Random;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Configures a CP subsystem of 3 CP members. When you run 3 instances of this
 * class, it will form the CP subsystem. Then, it fetches
 * a CP {@link FencedLock} proxy. Each CP member acquires the lock 2 times and
 * releases it afterwards. Between acquires and releases, it prints the fencing
 * tokens assigned to itself.
 */
public class CPMember {

    private static final int CP_MEMBER_COUNT = 3;

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        config.getCPSubsystemConfig().setCPMemberCount(CP_MEMBER_COUNT);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        FencedLock lock = hz.getCPSubsystem().getLock("lock");

        long fence1 = lock.lockAndGetFence();

        System.out.println("I acquired the lock for the first time at " + new Date() + " with fence: " + fence1);

        Thread.sleep(SECONDS.toMillis(1) + new Random().nextInt(100));

        long fence2 = lock.lockAndGetFence();

        System.out.println("I acquired the lock reentrantly with fence: " + fence2);

        Thread.sleep(SECONDS.toMillis(2));

        // calling unlock() two times since we acquired the lock 2 times

        System.out.println("Unlocking...");

        lock.unlock();

        System.out.println("I still hold the lock with fence: " + lock.getFence());

        Thread.sleep(SECONDS.toMillis(2));

        System.out.println("Unlocking again...");

        lock.unlock();

        System.out.println("Do I still hold the lock? " + lock.isLockedByCurrentThread());

        Thread.sleep(SECONDS.toMillis(30));

        // always destroy CP Subsystem data structures otherwise it can lead to a memory leak
        lock.destroy();

        hz.getLifecycleService().terminate();
    }

}
