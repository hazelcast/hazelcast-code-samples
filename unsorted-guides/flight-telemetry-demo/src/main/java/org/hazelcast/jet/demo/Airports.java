package org.hazelcast.jet.demo;

public class Airports {
    public static Double LONDON_AIRPORT_RADIUS = 10d;
    public static Double NEWYORK_AIRPORT_RADIUS = 10d;
    public static Double TOKYO_AIRPORT_RADIUS = 10d;

    // Thee London airports LCY, LGW and LHR
    public static Double LCY_LAT = 51.5048d;
    public static Double LCY_LON = 0.0495d;

    public static Double LGW_LAT = 51.1537d;
    public static Double LGW_LON = 0.1821d;

    public static Double LHR_LAT = 51.470020d;
    public static Double LHR_LON = -0.454295d;

    // Two Tokyo airports HND and NRT
    public static Double HND_LAT = 35.5235366d;
    public static Double HND_LON = 139.6987589d;

    public static Double NRT_LAT = 35.771991d;
    public static Double NRT_LON = 140.3906614d;

    // Three New york airports JFK, EWR and LGA
    public static Double JFK_LAT = 40.6413153d;
    public static Double JFK_LON = -73.780327d;

    public static Double EWR_LAT = 40.6895354d;
    public static Double EWR_LON = -74.1766511d;

    public static Double LGA_LAT = 40.7769311d;
    public static Double LGA_LON = -73.8761546d;


    public static boolean nearLCY(Double lon, Double lat) {
        return inBoundariesOf(lon, lat, boundingBox(LCY_LON, LCY_LAT, LONDON_AIRPORT_RADIUS));
    }

    public static boolean nearLGW(Double lon, Double lat) {
        return inBoundariesOf(lon, lat, boundingBox(LGW_LON, LGW_LAT, LONDON_AIRPORT_RADIUS));
    }

    public static boolean nearLHR(Double lon, Double lat) {
        return inBoundariesOf(lon, lat, boundingBox(LHR_LON, LHR_LAT, LONDON_AIRPORT_RADIUS));
    }

    public static boolean nearLGA(Double lon, Double lat) {
        return inBoundariesOf(lon, lat, boundingBox(LGA_LON, LGA_LAT, NEWYORK_AIRPORT_RADIUS));
    }

    public static boolean nearJFK(Double lon, Double lat) {
        return inBoundariesOf(lon, lat, boundingBox(JFK_LON, JFK_LAT, NEWYORK_AIRPORT_RADIUS));
    }

    public static boolean nearEWR(Double lon, Double lat) {
        return inBoundariesOf(lon, lat, boundingBox(EWR_LON, EWR_LAT, NEWYORK_AIRPORT_RADIUS));
    }

    public static boolean nearNRT(Double lon, Double lat) {
        return inBoundariesOf(lon, lat, boundingBox(NRT_LON, NRT_LAT, TOKYO_AIRPORT_RADIUS));
    }

    public static boolean nearHND(Double lon, Double lat) {
        return inBoundariesOf(lon, lat, boundingBox(HND_LON, HND_LAT, TOKYO_AIRPORT_RADIUS));
    }

    public static double[] boundingBox(Double lon, Double lat, Double radius) {
        double lat_rad = Math.abs(Math.cos(Math.toRadians(lat)) * 69);

        double boundingLon1 = lon + radius / lat_rad;
        double boundingLon2 = lon - radius / lat_rad;
        double boundingLat1 = lat + (radius / 69);
        double boundingLat2 = lat - (radius / 69);
        return new double[]{boundingLon1, boundingLat1, boundingLon2, boundingLat2};
    }

    public static boolean inBoundariesOf(Double lon, Double lat, double[] boundaries) {
        return !(lon > boundaries[0] || lon < boundaries[2]) &&
                !(lat > boundaries[1] || lat < boundaries[3]);
    }

    public static String getAirport(Double lon, Double lat) {
        if (nearLHR(lon, lat)) {
            return "London Heathrow";
        } else if (nearLCY(lon, lat)) {
            return "London City";
        } else if (nearLGW(lon, lat)) {
            return "London Gatwick";
        } else if (nearEWR(lon, lat)) {
            return "New York Newark";
        } else if (nearJFK(lon, lat)) {
            return "New York JFK";
        } else if (nearLGA(lon, lat)) {
            return "New York LaGuardia";
        } else if (nearHND(lon, lat)) {
            return "Tokyo Haneda";
        } else if (nearNRT(lon, lat)) {
            return "Tokyo Narita";
        }
        // unknown city
        return null;
    }

}
