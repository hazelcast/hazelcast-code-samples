package com.hazelcast.codesamples.cp.groupavailabilitylistener;

import com.hazelcast.cp.event.CPGroupAvailabilityEvent;
import com.hazelcast.cp.event.CPGroupAvailabilityListener;

public class LoggingGroupAvailabilityListener implements CPGroupAvailabilityListener {

    @Override
    public void availabilityDecreased(CPGroupAvailabilityEvent e) {
        System.out.println("Group Availability Decreased: " + e);
    }

    @Override
    public void majorityLost(CPGroupAvailabilityEvent e) {
        System.out.println("Group Majority Lost: " + e);
    }
}
