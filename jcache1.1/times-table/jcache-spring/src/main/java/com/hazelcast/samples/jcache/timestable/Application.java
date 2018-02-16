package com.hazelcast.samples.jcache.timestable;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.shell.Bootstrap;

/**
 * <p>Use Spring Boot and Spring Shell to build a Hazelcast
 * client instance from the {@code hazelcast-client.xml} file,
 * and make command line options available on it.
 * <p>
 * <p>Note the {@code @EnableCaching} annotation which
 * triggers Spring to make a cache manager.
 * </p>
 */
@EnableCaching
@SpringBootApplication
public class Application {

    /**
     * <p>Set Hazelcast logging type and JCache control
     * invariants and let Spring do the rest.
     * </p>
     *
     * @param args From command line
     */
    public static void main(String[] args) throws Exception {
        System.setProperty("hazelcast.jcache.provider.type", "client");
        System.setProperty("hazelcast.logging.type", "slf4j");
        System.setProperty("spring.cache.jcache.provider",
                "com.hazelcast.client.cache.impl.HazelcastClientCachingProvider");
        System.setProperty("spring.cache.type", "jcache");
        Bootstrap.main(args);
    }

}
