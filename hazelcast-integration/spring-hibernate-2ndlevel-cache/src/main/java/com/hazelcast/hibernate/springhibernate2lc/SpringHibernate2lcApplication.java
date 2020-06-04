package com.hazelcast.hibernate.springhibernate2lc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // needed just for the sake of SecondLevelCacheVisualizer
@SpringBootApplication(exclude = HazelcastAutoConfiguration.class) // in order to avoid autoconfiguring an extra Hibernate instance
public class SpringHibernate2lcApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringHibernate2lcApplication.class, args);
    }
}
