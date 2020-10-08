package com.hazelcast.codesamples.cp.groupavailabilitylistener;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * Configures a CP subsystem of 3 CP members. When you run 3 instances of this
 * class, it will form the CP subsystem.
 * <p>
 * If you terminate one of the processes, after some time remaining members
 * will print CP group availability decreased event to the console.
 * <p>
 * If you terminate one more process, after some time the remaining member
 * will print CP group majority lost event to the console.
 */
public class CPMember {

    private static final int CP_MEMBER_COUNT = 3;

    public static void main(String[] args) {
        Config config = new Config();
        config.getCPSubsystemConfig().setCPMemberCount(CP_MEMBER_COUNT);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        hz.getCPSubsystem().addGroupAvailabilityListener(new LoggingGroupAvailabilityListener());
    }
}
