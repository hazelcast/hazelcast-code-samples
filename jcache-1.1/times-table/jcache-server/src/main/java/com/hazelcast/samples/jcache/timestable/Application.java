package com.hazelcast.samples.jcache.timestable;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.Bootstrap;

/**
 * Use Spring Boot and Spring Shell to build a Hazelcast
 * server instance from the {@code hazelcast.xml} file,
 * and make command line options available on it.
 */
@SpringBootApplication
public class Application {

    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance hazelcastInstance() {
        return Hazelcast.newHazelcastInstance();
    }

    /**
     * Set Hazelcast logging type and let
     * Spring do the rest.
     *
     * @param args from the command line
     */
    public static void main(String[] args) throws Exception {
        System.setProperty("hazelcast.logging.type", "slf4j");
        Bootstrap.main(args);
    }
}
