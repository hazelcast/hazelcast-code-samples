package com.hazelcast.samples.json.jsongrid;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.YamlClientConfigBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>Create a Hazelcast client configuration object as
 * a Spring {@code @Bean}. Spring will deduce we want a
 * Hazelcast client and build it from this configuration.
 */
@Configuration
public class ApplicationConfig {

    /**
     * <p>Hazelcast configuration object, built from YML.
     * </p>
     *
     * @return Configuration for a Hazelcast client instance
     */
    @Bean
    public ClientConfig clientConfig() throws Exception {
        return new YamlClientConfigBuilder("hazelcast-client.yml").build();
    }
}
