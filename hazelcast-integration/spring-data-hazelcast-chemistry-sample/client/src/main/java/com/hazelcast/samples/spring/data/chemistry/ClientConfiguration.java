package com.hazelcast.samples.spring.data.chemistry;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring beans specific to a Hazelcast client-side process.
 */
@Configuration
public class ClientConfiguration {

    /**
     * Create a Hazelcast client and wrap it as a Spring bean.
     *
     * @return A Hazelcast client singleton
     */
    @Bean
    public HazelcastInstance hazelcastInstance() throws Exception {
        ClientConfig clientConfig = new XmlClientConfigBuilder("hazelcast-client.xml").build();

        return HazelcastClient.newHazelcastClient(clientConfig);
    }
}
