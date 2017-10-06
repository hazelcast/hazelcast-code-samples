package com.hazelcast.samples.nearcache.frauddetection;

import lombok.Getter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Capture the timestamp before testing begins.
 * <p>
 * The {@code @Order} annotation ensures this method
 * runs before the {@link FraudService} has been tested.
 */
@Component
@Order(Integer.MIN_VALUE)
@Getter
public class Before implements CommandLineRunner {

    private Instant before;

    @Override
    public void run(String... arg0) {
        before = Instant.now();
    }
}
