package com.hazelcast.samples.serialization.hazelcast.airlines;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.samples.serialization.hazelcast.airlines.ep.FlightLoadingEntryProcessor;
import com.hazelcast.samples.serialization.hazelcast.airlines.util.Constants;
import com.hazelcast.samples.serialization.hazelcast.airlines.util.FlightBuilder;

/**
 * <p>Commands additional to the defaults provided
 * by Spring Shell.
 * </p>
 * <p>Our commands are in upper case to make it easy
 * to distinguish from the built-ins.
 * </p>
 */
@Component
public class CLI implements CommandMarker {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    
    /**
     * <p>Bring a flight from wherever it is stored, potentially crossing the
     * network, to here. To print out.
     * </p>
     * 
     * @param code The flight code, Date is hardcoded
     * @return Flight if found
     */
    @CliCommand(value = "GET",
            help = "RETRIEVE A FLIGHT")
    public String get(
            @CliOption(key = {"CODE"}, mandatory = true) String code
    		) {
		IMap<MyKey, AbstractFlight> flightsMap = this.hazelcastInstance.getMap(Constants.IMAP_FLIGHTS);
    	
		MyKey myKey = new MyKey(code, Constants.WHEN);
		
		AbstractFlight flight = flightsMap.get(myKey);

		if (flight!=null) {
			return String.format("%s%n", flight);
		} else {
			return String.format("Flight '%s' does not exist%n", myKey);
		}
    }
    
    
    /**
     * <p>List the stored flights, keys are {@link java.lang.Comparable} so sort before printing.
     * </p>
     */
    @CliCommand(value = "KEYS",
            help = "DISPLAY THE STORED FLIGHTS")
    public String keys() {
    		IMap<MyKey, AbstractFlight> flightsMap = this.hazelcastInstance.getMap(Constants.IMAP_FLIGHTS);
    		
    		Set<MyKey> keys = flightsMap.keySet().stream().collect(Collectors.toCollection(TreeSet::new));
    		
    		keys.forEach(key -> System.out.println(" -> " + key));
    		
    		return String.format("[%d record%s]%n", keys.size(), (keys.size()==1 ? "" : "s"));
    }

    
    /**
     * <p>See how heavily loaded a flight is.
     * </p>
     * <p>This uses an {@link com.hazelcast.map.EntryProcessor EntryProcessor} to do the calculation
     * where the data is held, and return an {@link java.lang.Integer Integer}, 4 bytes, across the
     * network instead of the {@link AbstractFlight} object which is much larger.
     * </p>
     * 
     * @param code The flight code, Date is hardcoded
     * @return How many passengers
     */
    @CliCommand(value = "LOADING",
            help = "HOW MANY SEATS ARE IN USE ON A FLIGHT")
    public String loading(
            @CliOption(key = {"CODE"}, mandatory = true) String code
    		) {
		IMap<MyKey, AbstractFlight> flightsMap = this.hazelcastInstance.getMap(Constants.IMAP_FLIGHTS);
    	
		MyKey myKey = new MyKey(code, Constants.WHEN);
		
		if (flightsMap.containsKey(myKey)) {
			FlightLoadingEntryProcessor flightLoadingEntryProcessor = new FlightLoadingEntryProcessor();

			Integer count = (Integer) flightsMap.executeOnKey(myKey, flightLoadingEntryProcessor);

			return String.format("Flight '%s' has %d passenger%s%n", myKey, count, (count==1 ? "" : "s"));
		} else {
			return String.format("Flight '%s' does not exist%n", myKey);
		}
    }
    
    
    /**
     * <p>Load test data into the cluster.
     * </p>
     */
    @CliCommand(value = "TESTDATA",
            help = "INJECT TEST DATA INTO THE CLUSTER")
    public String testData() {
		IMap<MyKey, AbstractFlight> hazelcastMap = this.hazelcastInstance.getMap(Constants.IMAP_FLIGHTS);
		StringBuilder sb = new StringBuilder();

		MyKey myKey1 = new MyKey("HAZ001", Constants.WHEN);
		MyKey myKey2 = new MyKey("HAZ002", Constants.WHEN);
		MyKey myKey3 = new MyKey("HAZ003", Constants.WHEN);
		
    		V1Flight v1Flight = FlightBuilder.buildV1();
    		V2Flight v2Flight = FlightBuilder.buildV2();
    		V3Flight v3Flight = FlightBuilder.buildV3();

    		@SuppressWarnings("unused")
		Object previous = hazelcastMap.put(myKey1,v1Flight);
    		sb.append("Map.put('").append(myKey1).append("') returns previous value%n");
    		
    		hazelcastMap.set(myKey2,  v2Flight);
    		sb.append("Map.set('").append(myKey2).append("') is void method%n");
    		
    		Map<MyKey, AbstractFlight> ordinaryMap = new HashMap<>();
    		ordinaryMap.put(myKey3, v3Flight);
    		hazelcastMap.putAll(ordinaryMap);
    		sb.append("Map.putAll('").append(Arrays.asList(ordinaryMap.keySet())).append("') is void method%n");
    		
    		return String.format(sb.toString());
    }

}