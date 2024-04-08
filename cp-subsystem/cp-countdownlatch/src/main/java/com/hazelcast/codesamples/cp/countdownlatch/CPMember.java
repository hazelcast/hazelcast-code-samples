package com.hazelcast.codesamples.cp.countdownlatch;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.ICountDownLatch;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Configures a CP subsystem of 3 CP members. When you run 3 instances of this
 * class, it will form the CP subsystem. Then, it fetches
 * a CP {@link ICountDownLatch} proxy. Each CP member performs some dummy work
 * and decrements the count value. After that, it waits for all CP members to
 * complete their work.
 *
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class CPMember {

    private static final int CP_MEMBER_COUNT = 3;

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        config.getCPSubsystemConfig().setCPMemberCount(CP_MEMBER_COUNT);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        ICountDownLatch latch = hz.getCPSubsystem().getCountDownLatch("latch");

        latch.trySetCount(CP_MEMBER_COUNT);

        System.out.println("Starting to do some work");

        // do some sleeping to simulate doing something
        Thread.sleep(SECONDS.toMillis(5));

        System.out.println("Finished my work");

        // now we do a countdown which notifies all followers
        latch.countDown();

        System.out.println("Latch is decremented");

        boolean success = latch.await(60, SECONDS);

        if (success) {
            System.out.println("EVERYONE FINISHED THEIR WORK!");
        } else {
            System.out.println("SEEMS SOMEONE IS BEING LAZY :(");
        }

        Thread.sleep(SECONDS.toMillis(5));

        // always destroy CP Subsystem data structures otherwise it can lead to a memory leak
        latch.destroy();

        hz.shutdown();
    }
}
