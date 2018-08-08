package com.hazelcast.samples.kubernetes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>
 * Standard <a href="https://spring.io/projects/spring-boot">Spring Boot</a>
 * application launcher.
 * </p>
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
