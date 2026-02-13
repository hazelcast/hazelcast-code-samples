package com.hazelcast.samples;

import com.hazelcast.spring.HazelcastObjectExtractionConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = { HazelcastObjectExtractionConfiguration.class })
@Import(ShopConfiguration.class)
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
