package com.hazelcast.codesamples.cp.atomicreference;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicReference;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Configures a CP subsystem of 3 CP members. When you run 3 instances of this
 * class, it will form the CP subsystem. Then, it fetches
 * a CP {@link IAtomicReference} proxy and increments the integer value with
 * CAS {@link #NUMBER_OF_INCREMENTS} times.
 *
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class CPMember {

    private static final int CP_MEMBER_COUNT = 3;
    private static final int NUMBER_OF_INCREMENTS = 100;

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        config.getCPSubsystemConfig().setCPMemberCount(CP_MEMBER_COUNT);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        IAtomicReference<Integer> ref = hz.getCPSubsystem().getAtomicReference("reference");

        ref.compareAndSet(null, 0);

        for (int i = 0; i < NUMBER_OF_INCREMENTS; i++) {
            while (true) {
                int val = ref.get();
                if (ref.compareAndSet(val, val + 1)) {
                    break;
                }
            }
        }

        // Just wait for other instances to complete their operations...
        while (ref.get() < (CP_MEMBER_COUNT * NUMBER_OF_INCREMENTS)) {
            Thread.sleep(SECONDS.toMillis(1));
        }

        System.out.println("Ref value is " + ref.get());

        Thread.sleep(SECONDS.toMillis(5));

        // always destroy CP Subsystem data structures otherwise it can lead to a memory leak
        ref.destroy();

        hz.shutdown();
    }
}
