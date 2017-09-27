package com.hazelcast.samples.nearcache.frauddetection;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.monitor.NearCacheStats;

/**
 * <P>After the main processing has run, query Hazelcast
 * for counts of how many calls were made that the near-cache
 * helped with. If no near-cache is enabled the hit rate
 * will be zero, everything is a miss.
 * </P>
 * 
 * <P>The {@code @Order} annotation ensures this method
 * runs after the {@link FraudService} has been tested.
 * </P>
 */
@Component
@Order(Integer.MAX_VALUE)
public class After implements CommandLineRunner {

	@Autowired
	private Before before;
	@Autowired
	private HazelcastInstance hazelcastInstance;

	@Override
	public void run(String... arg0) throws Exception {
		
		Instant after = Instant.now();
		Duration duration = Duration.between(this.before.getBefore(), after);
		
		IMap<String, Airport> airportsMap = this.hazelcastInstance.getMap(MyConstants.MAP_NAME_AIRPORTS);

		NearCacheStats airportsMapNearCacheStats = 
				airportsMap.getLocalMapStats().getNearCacheStats();
		
		System.out.printf("===================================== %n");
		System.out.printf("===         R E S U L T S         === %n");
		System.out.printf("===================================== %n");
		System.out.printf("=== Map : '%s'%n", airportsMap.getName());

		System.out.printf("===  Calls............. : '%d'%n",
				FraudService.NUMBER_OF_TEST_ITERATIONS);

		if (airportsMapNearCacheStats!=null) {
			System.out.printf("===  Near-cache hits... : '%d'%n",
					airportsMapNearCacheStats.getHits());
			System.out.printf("===  Near-cache misses. : '%d'%n",
					airportsMapNearCacheStats.getMisses());
		}
		
		System.out.printf("===================================== %n");
		System.out.printf("===  Run time for tests : '%s'%n",
				duration);
		System.out.printf("===================================== %n");

	}

}
