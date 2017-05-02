package com.hazelcast.samples.eureka.partition.groups;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Start up a Hazelcast client.
 * <p>
 * We need the {@code @EnableDiscoveryClient} to create
 * the Eureka <b>client</b> that tries to connect to the
 * Eureka server instance(s) that are hopefully running.
 * <p>
 * The key point here is an Eureka client bean is
 * created, that connects to an external Eureka server or
 * server instances, based on the properties in the
 * YML files.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MyHazelcastClient implements CommandLineRunner {

    static {
        System.setProperty("spring.application.name", Constants.CLUSTER_NAME);
    }

    @Autowired
    private HazelcastInstance hazelcastInstance;

    /**
     * Create a Hazelcast client that connects to the
     * Hazelcast server(s), the {@link #run(String...)} method will
     * display contents of the maps, then we can shut down.
     *
     * @param args If you want to provide them, not needed
     */
    public static void main(String[] args) {
        SpringApplication.run(MyHazelcastClient.class, args);
        System.exit(0);
    }

    /**
     * Display the size of the maps, as simplistic proof
     * of the loss or preservation of contents.
     *
     * @param arg0 From {@link #main(String[])}
     */
    @Override
    public void run(String... arg0) throws Exception {
        String[] mapNames = new String[]{Constants.MAP_NAME_SAFE, Constants.MAP_NAME_UNSAFE};

        System.out.printf("--------------------------------------------------------------------------------%n");

        for (String mapName : mapNames) {
            IMap<?, ?> map = hazelcastInstance.getMap(mapName);

            System.out.printf("IMap: '%s', size==%d%n", map.getName(), map.size());
        }

        System.out.printf("--------------------------------------------------------------------------------%n");
    }
}
