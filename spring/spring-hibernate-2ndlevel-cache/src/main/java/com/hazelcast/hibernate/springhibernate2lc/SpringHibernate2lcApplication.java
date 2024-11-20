package com.hazelcast.hibernate.springhibernate2lc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

// needed just for the sake of SecondLevelCacheVisualizer
@EnableScheduling
@SpringBootApplication
@EnableCaching
@Import(HazelcastAutoConfiguration.class)
public class SpringHibernate2lcApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringHibernate2lcApplication.class, args);
    }
}
