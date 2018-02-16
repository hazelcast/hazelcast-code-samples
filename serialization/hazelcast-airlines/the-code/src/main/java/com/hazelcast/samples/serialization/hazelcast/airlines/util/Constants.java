package com.hazelcast.samples.serialization.hazelcast.airlines.util;

import java.time.LocalDate;

/**
 * <p>Utility constants
 * </p>
 */
public class Constants {
    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /* Where the data is stored in the data grid
     */
    public static final String IMAP_FLIGHTS = "flights";

    /* Hazelcast Airline's plane has 20 rows of 6 seats
     */
    public static final int ROWS = 20;
    public static final int SEATS = 6;

    /* Flight date is 12th December 2017, no confusion between YY-MM-DD or YY-DD-MM :-)
     */
    public static final LocalDate WHEN = LocalDate.parse("2017-12-12");

    /* Potential passenger names
     */
    public static final String[] PEOPLE = {
            "Chiara",
            "David",
            "Irene",
            "Jonathan",
            "Martin",
            "Neil",
            "Riaz",
            "Roger",
            "Greg",
            "Fuad",
            "Chris",
            "Enes",
            "Kevin",
            "Morgan",
            "Nadine",
            "Justin",
    };

    /* Unique ids for factories that build objects of different kinds
     */
    public static final int MY_DATASERIALIZABLE_FACTORY = 1;

    /* Unique ids for the objects built by the factories
     */
    public static final int V4FLIGHT_ID = 1;
    public static final int V5FLIGHT_ID = 1 + V4FLIGHT_ID;
    public static final int SEATRESERVERATIONENTRYPROCESSOR_ID = 1 + V5FLIGHT_ID;

}
