package com.hazelcast.spring.springaware;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.context.SpringManagedContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
class AppConfig {

    @Bean(name = "dummyBean")
    public IDummyBean dummyBean() {
        return new DummyBean();
    }

    @Bean
    public SpringManagedContext managedContext() {
        return new SpringManagedContext();
    }

    @Bean
    public HazelcastInstance instance() {
        Config config = new Config();
        config.setManagedContext(managedContext());
        return Hazelcast.newHazelcastInstance(config);
    }
}
