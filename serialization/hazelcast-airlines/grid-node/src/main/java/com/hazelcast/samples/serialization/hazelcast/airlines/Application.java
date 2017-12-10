package com.hazelcast.samples.serialization.hazelcast.airlines;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.Bootstrap;

/**
 * <p>Use Spring Boot and Spring Shell to build a Hazelcast
 * server instance from the {@code hazelcast.xml} file,
 * and make command line options available on it.
 * <p>
 */
@SpringBootApplication
public class Application {

    /**
     * <p>Set Hazelcast logging type and let
     * Spring do the rest.
     * </p>
     *
     * @param args From command line
     */
    public static void main(String[] args) throws Exception {
        System.setProperty("hazelcast.logging.type", "slf4j");
        Bootstrap.main(args);
    }

}
