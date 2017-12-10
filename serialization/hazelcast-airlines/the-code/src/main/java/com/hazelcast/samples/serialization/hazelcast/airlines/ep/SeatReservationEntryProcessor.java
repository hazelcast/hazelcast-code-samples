package com.hazelcast.samples.serialization.hazelcast.airlines.ep;

import java.io.IOException;
import java.util.Map.Entry;

import com.hazelcast.map.AbstractEntryProcessor;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.samples.serialization.hazelcast.airlines.AbstractFlight;
import com.hazelcast.samples.serialization.hazelcast.airlines.MyKey;
import com.hazelcast.samples.serialization.hazelcast.airlines.Person;
import com.hazelcast.samples.serialization.hazelcast.airlines.util.Constants;

import lombok.EqualsAndHashCode;

/**
 * <P>An {@link com.hazelcast.map.EntryProcessor EntryProcessor} that we use to do
 * delta processing. We want to set a value for one seat on the plane, but it's
 * much more efficient not to transfer a big object across the network. The
 * plane has 120 seats and we only want to reserve one.
 * </p>
 * <p>So we send the name of the person to the data, let it pick the first available
 * seat, and return the seat number.
 * </p>
 * <p>In order to send this code from where it is called to where it is run
 * we need some method of serialization. In this case we use
 * {@link com.hazelcast.nio.serialization.Portable Portable}.
 * </p>
 */
@SuppressWarnings("serial")
@EqualsAndHashCode(callSuper = false)
public class SeatReservationEntryProcessor extends AbstractEntryProcessor<MyKey, AbstractFlight> implements Portable {

    private String name;

    /**
     * <p>The {@code name} is the person to reserve the seat for.
     * </p>
     */
    public SeatReservationEntryProcessor() {
    }
    public SeatReservationEntryProcessor(String name) {
        this.name = name;
    }

    /**
     * <p>Implementing {@link com.hazelcast.map.AbstractEntryProcessor AbstractEntryProcessor}.</p>
     * <p>This code is executed on the master copy and any backup copies.
     * </p>
     * <p>As we do the same processing on both, they stay matching without having to
     * tansmit the value (which may be large).
     * </p>
     * <p>Only the processing applied to the master copy returns the result to
     * the caller.
     * </p>
     */
    @Override
    public String process(Entry<MyKey, AbstractFlight> entry) {

        // Who to seat
        Person person = new Person();
        person.setName(this.name.trim().toUpperCase());

        // Where to sit them
        AbstractFlight flight = entry.getValue();
        Person[][] rows = flight.getRows();

        /* Business logic, find the first seat from the back,
         * economy coach class.
         */
        int i = -1;
        int j = -1;
        String result = null;
        for (i = rows.length - 1 ; i >= 0 ; i--) {
            for (j = rows[i].length - 1 ; j >= 0 ; j--) {
                if (rows[i][j] == null && result == null) {
                    rows[i][j] = person;
                    result = String.format("Row %d Seat %s", i, Constants.ALPHABET.charAt(j));
                }
            }
        }

        // If the data has changed we need to tell Hazelcast
        if (result != null) {
            entry.setValue(flight);
        }


        // Null if not found
        return result;
    }

    /**
     * <p>Implementing {@link com.hazelcast.nio.serialization.Portable Portable}.</p>
     */
    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.name = portableReader.readUTF(Constants.FIELD_NAME_NAME);
    }

    /**
     * <p>Implementing {@link com.hazelcast.nio.serialization.Portable Portable}.</p>
     */
    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF(Constants.FIELD_NAME_NAME, this.name);
    }


    /**
     * <p>Implementing {@link com.hazelcast.nio.serialization.Portable Portable}.</p>
     */
    @Override
    public int getFactoryId() {
        return Constants.MY_PORTABLE_FACTORY;
    }


    /**
     * <p>Implementing {@link com.hazelcast.nio.serialization.Portable Portable}.</p>
     */
    @Override
    public int getClassId() {
        return Constants.SEATRESERVERATIONENTRYPROCESSOR_ID;
    }

}
