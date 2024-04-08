package com.hazelcast.codesamples.cp.atomiclong;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Configures a CP subsystem of 3 CP members. When you run 3 instances of this
 * class, it will form the CP subsystem. Then, it fetches
 * a CP {@link IAtomicLong} proxy and increments the counter value
 * {@link #NUMBER_OF_INCREMENTS} times.
 *
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class CPMember {

    private static final int CP_MEMBER_COUNT = 3;
    private static final int NUMBER_OF_INCREMENTS = 1000;

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        config.getCPSubsystemConfig().setCPMemberCount(CP_MEMBER_COUNT);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        IAtomicLong counter = hz.getCPSubsystem().getAtomicLong("counter");

        for (int i = 0; i < NUMBER_OF_INCREMENTS; i++) {
            long val = counter.incrementAndGet();
            if (i % 500 == 0) {
                System.out.println("At: " + i + " value: " + val);
            }
        }

        // Just wait for other instances to complete their operations...
        while (counter.get() < (CP_MEMBER_COUNT * NUMBER_OF_INCREMENTS)) {
            Thread.sleep(SECONDS.toMillis(1));
        }

        System.out.println("Counter value is " + counter.get());

        Thread.sleep(SECONDS.toMillis(5));

        // always destroy CP Subsystem data structures otherwise it can lead to a memory leak
        counter.destroy();

        hz.getLifecycleService().terminate();
    }
}
