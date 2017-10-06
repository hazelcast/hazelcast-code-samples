package com.hazelcast.samples.nearcache.frauddetection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Run a Spring Boot application. Spring Boot will
 * build a Hazelcast server instance as part of this
 * application, deducing that we want one from the
 * presence of the {@code hazelcast.xml} file and
 * Hazelcast classes on the classpath.
 * <p>
 * The {@link MyInitializer} class will load
 * some test data into the Hazelcast server grid.
 * Once started the server waits for client requests
 * for data.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
