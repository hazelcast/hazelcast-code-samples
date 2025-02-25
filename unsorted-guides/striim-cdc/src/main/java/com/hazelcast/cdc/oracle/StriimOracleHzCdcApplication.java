package com.hazelcast.cdc.oracle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.Random;

@SpringBootApplication
public class StriimOracleHzCdcApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StriimOracleHzCdcApplication.class);

    @Autowired
    private ProductInvRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(StriimOracleHzCdcApplication.class, args);
    }

    @Override
    public void run(String... args) {

        log.info("StriimOracleHzCdcApplication started.");
        log.info("Populating the database...");

        Random r = new Random();
        final long NANOSEC_PER_SEC = 1000l * 1000 * 1000;
        long startTime = System.nanoTime();
        while ((System.nanoTime() - startTime) < 60 * NANOSEC_PER_SEC) {
            repository.save(new ProductInv(r.nextDouble(), "product" + r.nextInt(), new Date()));
        }

        System.out.println("\nfindAll()");
        repository.findAll().forEach(System.out::println);

    }
}
