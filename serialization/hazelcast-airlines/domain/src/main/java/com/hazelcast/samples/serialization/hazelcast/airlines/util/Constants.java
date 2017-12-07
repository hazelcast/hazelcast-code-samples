package com.hazelcast.samples.serialization.hazelcast.airlines.util;

import java.time.LocalDate;

/**
 * <p>Utility constants
 * </p>
 */
public class Constants {

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
			"Justin"
	};
	
}
