package com.hazelcast.codesamples.cp.membershiplistener;

import com.hazelcast.cp.event.CPMembershipEvent;
import com.hazelcast.cp.event.CPMembershipListener;

public class LoggingCPMembershipListener implements CPMembershipListener {

    @Override
    public void memberAdded(CPMembershipEvent e) {
        System.out.println("CP Member Added: " + e);
    }

    @Override
    public void memberRemoved(CPMembershipEvent e) {
        System.out.println("CP Member Removed: " + e);
    }
}
