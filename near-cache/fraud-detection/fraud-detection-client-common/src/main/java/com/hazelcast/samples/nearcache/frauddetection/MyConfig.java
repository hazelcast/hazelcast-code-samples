package com.hazelcast.samples.nearcache.frauddetection;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration shared by clients, for establishing connectivity to the grid.
 */
@Configuration
public class MyConfig {

    /**
     * Create client configuration from the specified XML file.
     *
     * @return Configuration to build clients.
     */
    @Bean
    public ClientConfig clientConfig() throws Exception {
        return new XmlClientConfigBuilder("hazelcast-client.xml").build();
    }

    /**
     * Create and start a Hazelcast client using the
     * provided configuration.
     *
     * @param clientConfig The {@code @Bean} created above
     * @return An instance that is a client
     */
    @Bean
    public HazelcastInstance hazelcastInstance(ClientConfig clientConfig) {
        return HazelcastClient.newHazelcastClient(clientConfig);
    }
}
