package com.hazelcast.codesamples.cp.membershiplistener;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * Configures a CP subsystem of 3 CP members. When you run 3 instances of this
 * class, it will form the CP subsystem. After leader election is completed
 * in METADATA group, each member will print 3 added CP members to the console.
 * <p>
 * If you terminate one of the processes, after some time that CP member will be
 * automatically removed from CP subsystem and remaining members will print
 * the removed CP member to the console.
 */
public class CPMember {

    private static final int CP_MEMBER_COUNT = 3;
    private static final int AUTO_REMOVE_MISSING_MEMBER_SEC = 10;

    public static void main(String[] args) {
        Config config = new Config();
        config.getCPSubsystemConfig()
                .setCPMemberCount(CP_MEMBER_COUNT)
                .setSessionTimeToLiveSeconds(AUTO_REMOVE_MISSING_MEMBER_SEC)
                .setMissingCPMemberAutoRemovalSeconds(AUTO_REMOVE_MISSING_MEMBER_SEC);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        hz.getCPSubsystem().addMembershipListener(new LoggingCPMembershipListener());
    }
}
