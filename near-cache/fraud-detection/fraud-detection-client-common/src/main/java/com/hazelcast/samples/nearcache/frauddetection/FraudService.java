package com.hazelcast.samples.nearcache.frauddetection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.projection.Projection;
import com.hazelcast.projection.Projections;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.SqlPredicate;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <P>Shared amongst the clients, generates test data and validates it.
 * </P>
 * <P>Most of the work is in the {@link #test()} method.
 * </P>
 */
@Getter
@Service
@Slf4j
public class FraudService {

	static final int NUMBER_OF_TEST_ITERATIONS = 1_000_000;
	private static final int ALERT_LOGGING_LIMIT = 5;
	private static final long ONE_MINUTE_MS = 60 * 1000L;
	private static final int RADIUS_OF_THE_EARTH_M = 6_371_000;
	private static final long THREE_HOURS_MS = 3 * 60 * ONE_MINUTE_MS;
	private static final int USER_COUNT = 10;
	
	@Autowired
	private HazelcastInstance hazelcastInstance;

	private int alerts = 0;
	private int calls = 0;
	private IMap<String, Airport> airportsMap;
	private IMap<Integer, User> usersMap;
	private Random pseudoRandom;
	private String[] airportCodes;
	
	/**
	 * <P>Use the constructor to create a random number sequence
	 * with a seed taken from the build timestamp, which is in
	 * the format "{@code yyyyMMddHHmmss}" so looks like 
	 * "{@code 20170926140715}".
	 * </P>
	 * <P>This means when the client with and the client without
	 * the near-cache are run, they will use the same sequence
	 * of "random" numbers, as they were built at the same time.
	 * So their results are comparable in terms of data retrieval
	 * requests.
	 * </P>
	 * 
	 * @param buildTimestamp Set by <I>maven</I> at build time
	 * @throws Exception If the timestamp is not a number
	 */
	public FraudService(@Value("${build.timestamp}") String buildTimestamp) throws Exception {
		try {
			long seed = Long.parseLong(buildTimestamp);
			this.pseudoRandom = new Random(seed);
		} catch (NumberFormatException nfe) {
			String message = 
				String.format("Cannot parse '%s' as seed", buildTimestamp);
			log.error(message, nfe);
			throw new RuntimeException("Check 'application.properties' file post build");
		}
	}
	
	/**
	 * <P>Loop a fixed amount of times, randomly generated card transactions
	 * for users.
	 * </P>
	 * <UL>
	 * <LI><P>Randomly select a user for the transaction.
	 * </P></LI>
	 * <LI><P>Randomly select an airport for the transaction.
	 * </P></LI>
	 * <LI><P>Determine if this airport is unrealistically far away from the previous
	 * known location of that user, to be reached within three hours.
	 * </P></LI>
	 * <LI><P>Keep track of reads and suspicious transactions.
	 * </P></LI>
	 * </UL
	 */
	public void test() throws Exception {
				
		this.airportsMap = this.hazelcastInstance.getMap(MyConstants.MAP_NAME_AIRPORTS);
		this.usersMap = this.hazelcastInstance.getMap(MyConstants.MAP_NAME_USERS);
		
		// Ensure only one clients run at a time as they interfere on usersMap
		ILock iLock = hazelcastInstance.getLock("client"); 
		if (!iLock.tryLock(1, TimeUnit.SECONDS)) {
			System.err.printf("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! %n");
			System.err.printf("!!!           E R R O R           !!! %n");
			System.err.printf("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! %n");
			System.err.printf("!!! Another client is running %n");
			System.err.printf("!!! Need exclusive access to `userMap` %n");
			System.err.printf("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! %n");
		} else {
			try {

				// Remove output from previous run
				this.usersMap.clear();
				
				// Find the airports, sorted
				this.airportCodes = new TreeSet<String>(this.airportsMap.keySet())
						.stream().toArray(String[]::new);
				int airports = this.airportCodes.length;
				
				// Now, run the transaction simulator
				for (int i=1 ; i <= NUMBER_OF_TEST_ITERATIONS ; i++) {
					
					// Generate the user id and location for this transaction
					int userId = this.pseudoRandom.nextInt(USER_COUNT);
					String nextAirportCode = this.airportCodes[this.pseudoRandom.nextInt(airports)];
					
					// Retrieve the last known location for the user, or build if not known
					User user = this.usersMap.get(userId);
					if (user==null) {
						user = new User();
						user.setUserId(userId);
						user.setLastCardUsePlace(nextAirportCode);
						user.setLastCardUseTimestamp(System.currentTimeMillis());
					}
					
					// Create the new transaction, three hours after the previous
					long nextCardUsedTime = user.getLastCardUseTimestamp() + THREE_HOURS_MS;

					// Validate the new transaction
					boolean valid = this.validate(user, nextAirportCode, nextCardUsedTime);
					if (valid) {
						user.setLastCardUsePlace(nextAirportCode);
						user.setLastCardUseTimestamp(nextCardUsedTime);
						this.usersMap.set(user.getUserId(), user);
					} else {
						this.alert(user, nextAirportCode);
					}

					// Progress ticker
					if (i%25_000==0) {
						log.info("Test iteration {}/{}", i, NUMBER_OF_TEST_ITERATIONS);
					}
				}
				
			} catch (Exception e) {
				log.error("test()", e);
			} finally {
				iLock.unlock();
			}
		}
		
	}

	/**
	 * <P>Produce an alert when a fraudulent transaction is spotted.
	 * In this implementation we only produce this alert to the console
	 * for the first few occurrences, to avoid producing repeated output.
	 * </P>
	 * <P>We wish to log the airport descriptions rather than their
	 * codes. As we have the codes, we could do a {@code map.get(K)}
	 * call on the airports map. However, this would use the near
	 * cache if present so impact on measurements.
	 * </P>
	 * <P>Instead, we use a query to avoid influencing the near-cache
	 * stats. Queries cannot use the near-cache as the near-cache is
	 * not guaranteed to be a complete copy of the data, usually it
	 * will be a subset, which would give incomplete query results.
	 * </P>
	 * <P>As an added optimisation, use a projection so as only
	 * to retrieve the field we care about from matching objects,
	 * rather than the full object.
	 * </P>
	 * 
	 * @param user The person for whom fraud has been detected
	 * @param airportCode Where it was detected
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void alert(User user, String airportCode) {

		this.alerts++;

		if (this.alerts > ALERT_LOGGING_LIMIT) {
			return;
		}
		
		String sql = "code = '" + airportCode + "' OR code = '" + user.getLastCardUsePlace() + "'";
		
		Predicate predicate = new SqlPredicate(sql);
		
		Projection projection = Projections.singleAttribute("description");
		
		Collection results = this.airportsMap.project(projection, predicate);

		if (results.size()==2) {
			Iterator iterator = results.iterator();
			
			System.err.printf("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ %n");
			System.err.printf("~~~           A L E R T           ~~~ %n");
			System.err.printf("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ %n");
			System.out.printf("~~~ User : '%d'%n", user.getUserId());
			System.out.printf("~~~ Only three hours between card used at%n");
			while (iterator.hasNext()) {
				System.out.printf("~~~ %s%n", iterator.next());
			}
			System.err.printf("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ %n");
			
		} else {
			log.error("alert({}, {}), Collection.size()=={}", 
					user, airportCode, results.size());
		}
		
	}

	/**
	 * <P>Determine if a transaction is suspect based on whether it is
	 * feasible to have travelled from the previous location in the
	 * time elapsed.
	 * </P>
	 * <P>We use 800km / 500miles per hour as the speed of a plane
	 * as the basis for this decision.
	 * </P>
	 * <P><B>Near-cache<B> : this method retrieves the two locations
	 * (previous airport and this airport) from Hazelcast, which will
	 * use the near-cache if one has been defined. The code is unaware
	 * if the near-cache is used.
	 * </P>
	 * 
	 * @param user
	 * @param nextAirportCode
	 * @param nextCardUsedTime
	 * @return True if the distance is feasible within 3 hours
	 */
	private boolean validate(User user, String nextAirportCode, long nextCardUsedTime) {
		
		if (nextAirportCode.equals(user.getLastCardUsePlace())) {
			return true;
		}

		// ----------------------------------------------------
		// Retrieve airport locations, POSSIBLY USES NEAR-CACHE
		// ----------------------------------------------------
		Airport lastAirport = this.airportsMap.get(user.getLastCardUsePlace());
		Airport nextAirport = this.airportsMap.get(nextAirportCode);
		this.calls += 2;

		// Time
		double minutes = (nextCardUsedTime - user.getLastCardUseTimestamp()) / ONE_MINUTE_MS;
		
		// Distance
		double metres = haversine(nextAirport.getLatitude(), nextAirport.getLongitude(), 
				lastAirport.getLatitude(), lastAirport.getLongitude());
		  
		// Speed = Distance / Time
		double speed = metres / minutes;
		
		// 800 km/hr == 1300 m/min
		if (speed > 13000) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * <P>Calculate the distance between two points on the surface of the
	 * earth using the
	 * <A HREF="https://en.wikipedia.org/wiki/Haversine_formula">Haversine_formula</A>
	 * </P>
	 * <P>We use a rough value for the radius of the Earth, 6371km / 3958 miles,
	 * remembering the Earth is not exactly round so the radius varies.
	 * </P>
	 * <P>Numeric precision on double, etc means the result is not absolutely
	 * accurate, but good enough.
	 * </P>
	 * 
	 * @param latitude of point 1 in degrees
	 * @param longitude of point 1 in degrees
	 * @param latitude2 of point 2 in degrees
	 * @param longitude2 of point 2 in degrees
	 * @return The distance in metres (1 metre = 39 inches)
	 */
	private static double haversine(double latitude, double longitude, double latitude2, double longitude2) {

		double lat1 = Math.toRadians(latitude);
		double lat2 = Math.toRadians(latitude2);
		double long1 = Math.toRadians(longitude);
		double long2 = Math.toRadians(longitude2);
		
		double latDiff = lat1 - lat2;
		double longDiff = long1 - long2;
		
		double hav= Math.pow(Math.sin(latDiff / 2), 2)
		 + Math.pow(Math.sin(longDiff / 2), 2)
		 * Math.cos(lat1)
		 * Math.cos(lat2);
		
		return 2 * RADIUS_OF_THE_EARTH_M * Math.asin(Math.sqrt(hav));
	}
	
}
