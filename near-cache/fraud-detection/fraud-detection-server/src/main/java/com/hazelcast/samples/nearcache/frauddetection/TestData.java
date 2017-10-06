package com.hazelcast.samples.nearcache.frauddetection;

/**
 * Test data here is the locations of some airports.
 */
public class TestData {

    public static final Object[][] AIRPORTS = new Object[][]{
            // Code, Name, Latitude (N), Longitude (E)
            {"AMS", "Amsterdam", 52.3105, 4.7683},
            {"BCN", "Barcelona", 41.2974, 2.0833},
            {"BRU", "Brussels", 50.9010, 4.4856},
            {"CDG", "Paris Charles De Gaulle", 49.0097, 2.5479},
            {"CPT", "Cape Town International", -33.9715, 18.6021},
            {"DCA", "Washington National", 38.8512, -77.0402},
            {"EWR", "New York Newark", 40.6895, -74.1745},
            {"FRA", "Frankfurt", 50.0379, 8.5622},
            {"GVA", "Geneva", 46.2370, 6.1092},
            {"IAD", "Washington Dulles", 38.9531, -77.4565},
            {"JFK", "New York John F Kennedy", 40.6413, -73.7781},
            {"LCY", "London City", 51.5048, 0.0495},
            {"LHR", "London Heathrow", 51.47, -0.4543},
            {"LGA", "New York LaGuardia", 40.7769, -73.8740},
            {"LGW", "London Gatwick", 51.1537, -0.1821},
            {"LTN", "London Luton", 51.8763, -0.3717},
            {"MAD", "Madrid", 40.4839, -3.5680},
            {"MUC", "Munich", 48.3537, 11.7750},
            {"STN", "London Stansted", 51.8860, 0.2389},
            {"ZRH", "Zurich", 47.4582, 8.5555},
    };
}
