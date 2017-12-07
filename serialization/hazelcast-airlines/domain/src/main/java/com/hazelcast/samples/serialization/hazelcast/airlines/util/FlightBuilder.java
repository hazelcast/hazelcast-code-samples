package com.hazelcast.samples.serialization.hazelcast.airlines.util;

import com.hazelcast.samples.serialization.hazelcast.airlines.AbstractFlight;
import com.hazelcast.samples.serialization.hazelcast.airlines.Person;
import com.hazelcast.samples.serialization.hazelcast.airlines.V1Flight;
import com.hazelcast.samples.serialization.hazelcast.airlines.V2Flight;
import com.hazelcast.samples.serialization.hazelcast.airlines.V3Flight;

/**
 * <p>Build flight objects.
 * </p>
 */
public class FlightBuilder {

	/**
	 * <p>Same people on every flight
	 * </p>
	 * 
	 * @param flight The object to inject into
	 * @param code Part of the key
	 */
	private static void populate(AbstractFlight flight, String code) {
		flight.setCode(code);
		flight.setDate(Constants.WHEN);
		Person[][] rows = new Person[Constants.ROWS][Constants.SEATS];
		Helpers.loadRows(rows);
		flight.setRows(rows);
	}

	public static V1Flight buildV1() {
		V1Flight v1Flight = new V1Flight();
		FlightBuilder.populate(v1Flight, "HAZ001");
		return v1Flight;
	}

	public static V2Flight buildV2() {
		V2Flight v2Flight = new V2Flight();
		FlightBuilder.populate(v2Flight, "HAZ002");
		return v2Flight;
	}

	public static V3Flight buildV3() {
		V3Flight v3Flight = new V3Flight();
		FlightBuilder.populate(v3Flight, "HAZ003");
		return v3Flight;
	}

}
