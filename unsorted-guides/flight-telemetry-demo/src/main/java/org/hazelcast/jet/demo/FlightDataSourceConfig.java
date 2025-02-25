package org.hazelcast.jet.demo;

import java.util.Map;

public class FlightDataSourceConfig {

    /* The methods in this class return a Java Map containing configuration for the jobs submitted to Hazelcast.

       Each entry in the maps results in a new job being submitted, the config returned defines the query lat, log and
       radius used to query the ADS-B exchange API. The bigger the radius the more bandwidth required to run the demo.
     */
    public static Map<String, Map<String, String>> getLowBandwidthConfig() {

        /* This function returns config for 3 jobs each results in a query of the ADS-B exchange API (be careful of your
           quota and spend) however the tight radius means less bandwidth is required to run the demo. */
        return Map.of(
                "London", Map.of(
                        // Setting to radar radius to 70 KM to pickup traffic for the three main London airports
                        // (distance between airports is
                        //      39K between LHR and LGW,
                        //      42KM between LGW and LCY and
                        //      35KM between LCY and LHR)
                        "RADAR_RADIUS", "70", // Half max distance + 10KM

                        // This is the lat long for North London Cricket Club which is the center point of all London airports
                        "RADAR_LAT", "51.5798373",
                        "RADAR_LON", "-0.1387518",

                        // Floor is the minimum altitude we consider a flight for altitude trend analysis
                        // we want to detect aircraft off the ground but still ascending (usually 100
                        // + the altitude of the highest airport - Gatwick is 62M/203ft)
                        // Ceiling is the maximum altitude we consider a flight for altitude trend analysis (usually 3000 + the floor)
                        "FLOOR", "303",
                        "CEILING", "3303"
                ),
                "New York", Map.of(
                        // Setting to radar radius to 70 KM to pickup traffic for the three main New York airports
                        // (distance between airports is
                        //      33K between EWR and JFK,
                        //      16KM between JFK and LGA and
                        //      27KM between LGA and EWR)
                        "RADAR_RADIUS", "70", // Half max distance + 10KM

                        // This is the lat long for 16 Manhattan Ave, Brooklyn, NY 11206 an undistinguished address in Manhattan
                        // which is the approximate center point of JFK, EWR and LGA airports
                        "RADAR_LAT", "40.702654",
                        "RADAR_LON", "-73.944399",

                        // Floor is the minimum altitude we consider a flight for altitude trend analysis
                        // we want to detect aircraft off the ground but still ascending (usually 100
                        // + the altitude of the highest airport - LaGuardia is 6M/20ft)
                        // Ceiling is the maximum altitude we consider a flight for altitude trend analysis (usually 3000 + the floor)
                        "FLOOR", "120",
                        "CEILING", "3120"
                ),
                "Tokyo", Map.of(
                        // Setting to radar radius to 70 KM to pickup traffic for all Tokyo airports
                        // (distance between airports is
                        //      65K between NRT and HND)
                        "RADAR_RADIUS", "80",  // Half max distance + 10KM

                        // This is the lat long for Kaihimmakuhari Station
                        // which is the approximate center point of HND and NRT airports
                        "RADAR_LAT", "35.6484242",
                        "RADAR_LON", "140.0394978",

                        // Floor is the minimum altitude we consider a flight for altitude trend analysis
                        // we want to detect aircraft off the ground but still ascending (usually 100
                        // + the altitude of the highest airport - Narita is 43M/141ft)
                        // Ceiling is the maximum altitude we consider a flight for altitude trend analysis (usually 3000 + the floor)
                        "FLOOR", "241",
                        "CEILING", "3241"
                )
        );
    }

    public static Map<String, Map<String, String>> getHighBandwidthConfig() {

        /* This function returns config for 3 jobs each results in a query of the ADS-B exchange API (be careful of your
           quota and spend) the mid-sized radius balances bandwidth use, processing but still results in multiple API
           jobs and API calls for each region. */
        return Map.of(
                "Europe", Map.of(
                        // Setting to radar radius to 3289 KM to pickup approach traffic around europe see https://www.freemaptools.com/radius-around-point.htm
                        "RADAR_RADIUS", "3289",
                        "RADAR_LAT", "17.300558",
                        "RADAR_LON", "25.502798",
                        // We cant adjust for locality to we set the floor and ceiling based on sea level
                        "FLOOR", "0",
                        "CEILING", "3000"
                ),

                "AsiaPacific", Map.of(
                        // Setting to radar radius to 45021 KM to pickup traffic around asia see https://www.freemaptools.com/radius-around-point.htm
                        "RADAR_RADIUS", "45021",
                        "RADAR_LAT", "7.036128",
                        "RADAR_LON", "119.792693",
                        // We cant adjust for locality to we set the floor and ceiling based on sea level
                        "FLOOR", "0",
                        "CEILING", "3000"
                ),
                "Americas", Map.of(
                        // Setting to radar radius to 4680 KM to pickup traffic around the americas see https://www.freemaptools.com/radius-around-point.htm
                        "RADAR_RADIUS", "4680",
                        "RADAR_LAT", "9.500888",
                        "RADAR_LON", "-93.402480",
                        // We cant adjust for locality to we set the floor and ceiling based on sea level
                        "FLOOR", "0",
                        "CEILING", "3000"
                )
        );
    }

    public static Map<String, Map<String, String>> getMediumBandwidthConfig() {

        /* This function returns config for 1 job to query of the ADS-B exchange API (be careful of your
           quota and spend) the large radius increases bandwidth and processing but results in fewer API calls. */
        return Map.of(
                "Global", Map.of(
                        // Setting to radar radius to capture all traffic
                        "RADAR_RADIUS", "15000",

                        // This is the lat long for North London Cricket Club
                        "RADAR_LAT", "51.5798373",
                        "RADAR_LON", "-0.1387518",
                        // We cant adjust for locality to we set the floor and ceiling based on sea level
                        "FLOOR", "0",
                        "CEILING", "3000"
                )
        );
    }


}
