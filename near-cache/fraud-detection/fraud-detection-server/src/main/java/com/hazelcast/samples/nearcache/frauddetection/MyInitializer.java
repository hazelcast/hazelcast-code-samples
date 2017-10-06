package com.hazelcast.samples.nearcache.frauddetection;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initialize the cluster, ensuring all maps are created
 * which is useful for monitoring, and injecting test
 * data.
 */
@Component
@Slf4j
public class MyInitializer implements CommandLineRunner {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    /**
     * Access all maps, which creates them if they don't already exist. This means all
     * maps are visible on the management center if that is being used, even if they have
     * never had any content added.
     * <p>
     * Call the {@link #loadAirports()} method to populate the "{@code airports}" map.
     */
    @Override
    public void run(String... arg0) {
        // Initialise all maps
        for (String mapName : MyConstants.MAP_NAMES) {
            hazelcastInstance.getMap(mapName);
        }

        loadAirports();
    }

    /**
     * Load the airport data into the airport map, once per cluster.
     */
    private void loadAirports() {
        IMap<String, Airport> airportsMap = hazelcastInstance.getMap(MyConstants.MAP_NAME_AIRPORTS);

        if (!airportsMap.isEmpty()) {
            log.info("Skip loading '{}', not empty", airportsMap.getName());
        } else {
            for (int i = 0; i < TestData.AIRPORTS.length; i++) {
                Object[] airportData = TestData.AIRPORTS[i];

                Airport airport = new Airport();

                airport.setCode(airportData[0].toString());
                airport.setDescription(airportData[1].toString());
                airport.setLatitude(Double.parseDouble(airportData[2].toString()));
                airport.setLongitude(Double.parseDouble(airportData[3].toString()));

                airportsMap.put(airport.getCode(), airport);
            }

            log.info("Loaded {} into '{}'", TestData.AIRPORTS.length, airportsMap.getName());
        }
    }
}
