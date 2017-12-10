package com.hazelcast.samples.serialization.hazelcast.airlines.util;

import com.hazelcast.samples.serialization.hazelcast.airlines.AbstractFlight;
import com.hazelcast.samples.serialization.hazelcast.airlines.Person;
import com.hazelcast.samples.serialization.hazelcast.airlines.V1Flight;
import com.hazelcast.samples.serialization.hazelcast.airlines.V2Flight;
import com.hazelcast.samples.serialization.hazelcast.airlines.V3Flight;
import com.hazelcast.samples.serialization.hazelcast.airlines.V4Flight;
import com.hazelcast.samples.serialization.hazelcast.airlines.V5Flight;

/**
 * <p>Build flight objects.
 * </p>
 */
public class FlightBuilder {

    /**
     * <p>Same people on every flight, all flights on the same
     * day.
     * </p>
     * <p>Because Hazelcast is so fast, our one plane can manage
     * to do all these flights.
     * </p>
     *
     * @param flight The object to inject into
     * @param code Part of the key
     */
    //CHECKSTYLE:OFF
	private static void populate(AbstractFlight flight, String code) {
    //CHECKSTYLE:ON
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

    public static V4Flight buildV4() {
        V4Flight v4Flight = new V4Flight();
        FlightBuilder.populate(v4Flight, "HAZ004");
        return v4Flight;
    }

    public static V5Flight buildV5() {
        V5Flight v5Flight = new V5Flight();
        FlightBuilder.populate(v5Flight, "HAZ005");
        return v5Flight;
    }

}
