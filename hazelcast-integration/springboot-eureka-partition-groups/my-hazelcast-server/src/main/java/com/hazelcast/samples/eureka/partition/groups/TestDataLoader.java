package com.hazelcast.samples.eureka.partition.groups;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.spi.properties.GroupProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Load some test data into the cluster at start-up.
 */
@Component
@Slf4j
public class TestDataLoader implements CommandLineRunner {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    /**
     * Since the tests should see the maps lose some or
     * no data, but not all data, we can use the emptiness
     * of the maps as the indicator that data load hasn't
     * happened at all.
     * <p>
     * There is a minor race condition here as we test
     * the size of the maps then load the data, but since
     * the test data is preset it wouldn't matter if two
     * nodes loaded it at once.
     */
    @Override
    public void run(String... arg0) throws Exception {
        IMap<Integer, String> safeMap = hazelcastInstance.getMap(Constants.MAP_NAME_SAFE);
        IMap<Integer, String> unsafeMap = hazelcastInstance.getMap(Constants.MAP_NAME_UNSAFE);

        int partitionCount = Integer.valueOf(System.getProperty(GroupProperty.PARTITION_COUNT.getName(),
                GroupProperty.PARTITION_COUNT.getDefaultValue()));

        log.info("\n--------------------------------------------------------------------------------");

        if (safeMap.size() != 0 || unsafeMap.size() != 0) {
            log.info("Data exists in cluster, skipping load");
        } else {
            for (int i = 0; i < partitionCount; i++) {
                safeMap.set(i, "safe" + i);
                unsafeMap.set(i, "unsafe" + i);
            }
            log.info("IMap: '{}'.size()=={}", safeMap.getName(), safeMap.size());
            log.info("IMap: '{}'.size()=={}", unsafeMap.getName(), unsafeMap.size());
        }

        log.info("\n--------------------------------------------------------------------------------");
    }
}
