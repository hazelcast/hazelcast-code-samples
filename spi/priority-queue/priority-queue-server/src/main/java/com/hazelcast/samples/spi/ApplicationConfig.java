package com.hazelcast.samples.spi;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.ServiceConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring beans to encapsulate Hazelcast.
 * <p>
 * If we did this all in the "{@code hazelcast.xml}"
 * then Spring Boot would figure out the code below.
 * Code it by hand to make it clearer what is happening.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Load Hazelcast configuration from the standard
     * file "{@code hazelcast.xml}" then extend it with
     * the custom service.
     *
     * @return Configuration to build a Hazelcast server
     */
    @Bean
    public Config config() {
        Config config = new ClasspathXmlConfig("hazelcast.xml");

        // Record the custom service class
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setEnabled(true);
        serviceConfig.setName(MyPriorityQueueService.class.getSimpleName());
        serviceConfig.setClassName(MyPriorityQueueService.class.getName());

        // Add it to overall config
        config.getServicesConfig().addServiceConfig(serviceConfig);

        return config;
    }

    /**
     * Create a Hazelcast server instance configured
     * as provided.
     *
     * @param config The {@code @Bean} created above
     * @return A Hazelcast server, as a Spring {@code @Bean}
     */
    @Bean
    public HazelcastInstance hazelcastInstance(Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }
}
