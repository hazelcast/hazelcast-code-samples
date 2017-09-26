package com.hazelcast.samples.nearcache.frauddetection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import lombok.extern.slf4j.Slf4j;

/**
 * <P>Initialize the cluster, ensuring all maps are created
 * which is useful for monitoring, and injecting test
 * data.
 * </P>
 */
@Component
@Slf4j
public class MyInitializer implements CommandLineRunner {

	@Autowired
	private HazelcastInstance hazelcastInstance;
	
	/**
	 * <P>Access all maps, which creates them if
	 * they don't already exist. This means all
	 * maps are visible on the management centre
	 * if that is being used, even if they have
	 * never had any content added.
	 * </P> 
	 * <P>Call the {@link #loadAirports()} method to
	 * populate the "{@code airports}" map.
	 * </P>
	 */
	@Override
	public void run(String... arg0) throws Exception {
		
		// Initialise all maps
		for (String mapName : MyConstants.MAP_NAMES) {
			this.hazelcastInstance.getMap(mapName);
		}

		this.loadAirports();
	}

	/**
	 * <P>Load the airport data into the airport map, once
	 * per cluster.
	 * </P>
	 */
	private void loadAirports() throws Exception {
		IMap<String, Airport> airportsMap = 
				this.hazelcastInstance.getMap(MyConstants.MAP_NAME_AIRPORTS);
		
		if (!airportsMap.isEmpty()) {
			log.info("Skip loading '{}', not empty", airportsMap.getName());
		} else {

			for (int i=0; i< TestData.AIRPORTS.length; i++) {
				Object[] airportData = TestData.AIRPORTS[i];
				
				Airport airport = new Airport();
				
				airport.setCode(airportData[0].toString());
				airport.setDecription(airportData[1].toString());
				airport.setLatitude(Double.parseDouble(airportData[2].toString()));
				airport.setLongitude(Double.parseDouble(airportData[3].toString()));
				
				airportsMap.put(airport.getCode(), airport);
			}

			log.info("Loaded {} into '{}'", TestData.AIRPORTS.length, airportsMap.getName());
		}
	}

}
