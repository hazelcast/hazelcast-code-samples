package com.hazelcast.samples.nearcache.frauddetection;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.monitor.NearCacheStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * After the main processing has run, query Hazelcast
 * for counts of how many calls were made that the Near Cache
 * helped with. If no Near Cache is enabled the hit rate
 * will be zero, everything is a miss.
 * <p>
 * The {@code @Order} annotation ensures this method
 * runs after the {@link FraudService} has been tested.
 */
@Component
@Order
public class After implements CommandLineRunner {

    @Autowired
    private Before before;
    @Autowired
    private FraudService fraudService;
    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Override
    public void run(String... arg0) {
        Instant after = Instant.now();
        Duration duration = Duration.between(before.getBefore(), after);

        IMap<String, Airport> airportsMap = hazelcastInstance.getMap(MyConstants.MAP_NAME_AIRPORTS);

        NearCacheStats airportsMapNearCacheStats = airportsMap.getLocalMapStats().getNearCacheStats();

        System.out.printf("===================================== %n");
        System.out.printf("===         R E S U L T S         === %n");
        System.out.printf("===================================== %n");
        System.out.printf("=== Map : '%s'%n", airportsMap.getName());

        System.out.printf("===  Calls............. : '%d'%n", fraudService.getCalls());
        System.out.printf("===  Alerts............ : '%d'%n", fraudService.getAlerts());

        if (airportsMapNearCacheStats != null) {
            System.out.printf("===  Near Cache hits... : '%d'%n", airportsMapNearCacheStats.getHits());
            System.out.printf("===  Near Cache misses. : '%d'%n", airportsMapNearCacheStats.getMisses());
        }

        System.out.printf("===================================== %n");
        System.out.printf("===  Run time for tests : '%s'%n", duration);
        System.out.printf("===================================== %n");
    }
}
