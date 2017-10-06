package com.hazelcast.samples.nearcache.frauddetection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This runs the client <b>without</b> a Near Cache configured
 * in its {@code hazelcast-client.xml} file.
 * <p>
 * All the work is done by the {@link FraudService.test()}
 * method so this is the same for both clients.
 * <p>
 * <b>Note:</b> This class {@code Application.java} is
 * <u>exactly</u> the same as the {@code Application.java}
 * for the client with the Near Cache. It would be better
 * to use one class in both modules, but Spring Boot's
 * packaging is easiest if the class with the {code main()}
 * method is in the top level.
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private FraudService fraudService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.exit(0);
    }

    @Override
    public void run(String... arg0) throws Exception {
        this.fraudService.test();
    }
}
