package com.hazelcast.samples.spring.data.chemistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Run a Hazelcast client as a REST application. Data can be inspected
 * using simple URL calls in a browser.
 *
 * Use <a href="http://localhost:8080/">http://localhost:8080/</a>
 * to discover what URLs are available.
 */
@SpringBootApplication
public class Client {

    /**
     * Launch Spring Boot.
     *
     * @param args From the O/s to pass on
     */
    public static void main(String[] args) {
        SpringApplication.run(Client.class, args);
    }
}
