package com.hazelcast.samples.serialization.hazelcast.airlines.ep;

import java.util.Map.Entry;

import com.hazelcast.core.ReadOnly;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.samples.serialization.hazelcast.airlines.AbstractFlight;
import com.hazelcast.samples.serialization.hazelcast.airlines.MyKey;
import com.hazelcast.samples.serialization.hazelcast.airlines.Person;
import com.hazelcast.samples.serialization.hazelcast.airlines.util.Helpers;

/**
 * <p>An {@link com.hazelcast.map.EntryProcessor EntryProcessor} is code that
 * is executed on data records where they reside (ie. on the grid) and
 * returns an answer.
 * </p>
 * <p>This one counts how many seats are occupied on the plane, the so-called
 * "<i>loading factor</i>".  We mark this as {@link com.hazelcast.core.ReadOnly ReadOnly}
 * since we know the data isn't changed by this code and it helps Hazelcast to know
 * that - {@code ReadOnly} calls don't need to care about locks.
 * </p>
 * <p>Indirectly this pulls in {@link java.io.Serializable} as a mechanism for the
 * class fields (of which there are none) to be transferred from caller to callee.
 * </p>
 */
@SuppressWarnings("serial")
public class FlightLoadingEntryProcessor implements EntryProcessor<MyKey, AbstractFlight>, ReadOnly {

    /**
     * <p>Count how many seats are allocated.
     * </p>
     *
     * @param entry A flight
     * @return How many seats are taken
     */
    @Override
    public Integer process(Entry<MyKey, AbstractFlight> entry) {
        AbstractFlight flight = entry.getValue();
        Person[][] rows = flight.getRows();
        return Helpers.countOccupied(rows);
    }

    /**
     * <p>Null as {@link com.hazelcast.core.ReadOnly ReadOnly} means
     * the data is not mutated, nothing is applied to backup copies.
     * </p>
     */
    @Override
    public EntryBackupProcessor<MyKey, AbstractFlight> getBackupProcessor() {
        return null;
    }

}
