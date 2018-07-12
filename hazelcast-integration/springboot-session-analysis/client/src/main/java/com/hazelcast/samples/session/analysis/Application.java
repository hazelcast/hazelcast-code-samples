package com.hazelcast.samples.session.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>Allow Spring Boot to create the necessary scaffolding to
 * run this application as a web app.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        System.setProperty("my.group.name", Constants.MY_GROUP_NAME);

        SpringApplication.run(Application.class, args);
    }

}
