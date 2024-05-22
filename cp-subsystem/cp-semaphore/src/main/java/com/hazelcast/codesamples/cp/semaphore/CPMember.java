package com.hazelcast.codesamples.cp.semaphore;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.ISemaphore;

import java.util.Date;
import java.util.Random;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Configures a CP subsystem of 3 CP members. When you run 3 instances of this
 * class, it will form the CP subsystem. Then, it fetches
 * a CP {@link ISemaphore} proxy, which is initialized with
 * {@code CP_MEMBER_COUNT - 1}. Each CP member acquires and releases a permit
 * {@link #NUMBER_OF_ROUNDS} times. At any time, {@code CP_MEMBER_COUNT - 1}
 * CP members will be holding a permit and the last member will be waiting for
 * one of them to release a permit.
 *
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class CPMember {

    private static final int CP_MEMBER_COUNT = 3;
    private static final int NUMBER_OF_ROUNDS = 2;

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        config.getCPSubsystemConfig().setCPMemberCount(CP_MEMBER_COUNT);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        ISemaphore semaphore = hz.getCPSubsystem().getSemaphore("semaphore");

        semaphore.init(CP_MEMBER_COUNT - 1);

        for (int i = 0; i < NUMBER_OF_ROUNDS; i++) {
            Thread.sleep(SECONDS.toMillis(1) + new Random().nextInt(100));

            semaphore.acquire();

            System.out.println("I acquired a permit at " + new Date());

            Thread.sleep(SECONDS.toMillis(5));

            semaphore.release();

            System.out.println("I released the permit!");
        }

        Thread.sleep(SECONDS.toMillis(30));

        // always destroy CP Subsystem data structures otherwise it can lead to a memory leak
        semaphore.destroy();

        hz.shutdown();

    }
}
