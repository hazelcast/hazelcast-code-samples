package com.hazelcast.samples.serialization.hazelcast.airlines.util;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import com.hazelcast.samples.serialization.hazelcast.airlines.V5Flight;
import com.hazelcast.samples.serialization.hazelcast.airlines.ep.SeatReservationEntryProcessor;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>On the receiving process, builds {@link com.hazelcast.nio.serialization.Portable
 * Portable} objects based on the code number sent by the sending process.
 * </p>
 */
@Slf4j
public class MyPortableFactory implements PortableFactory {

    @Override
    public Portable create(int id) {
        switch (id) {
            case Constants.V5FLIGHT_ID: return new V5Flight();
            case Constants.SEATRESERVERATIONENTRYPROCESSOR_ID: return new SeatReservationEntryProcessor();
            default: log.error("create({}), unknown code", id);
                     return null;
        }
    }

}
