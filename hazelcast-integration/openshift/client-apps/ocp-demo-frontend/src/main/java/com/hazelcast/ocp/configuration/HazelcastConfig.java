package com.hazelcast.ocp.configuration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@Configuration
@Profile("dev")
public class HazelcastConfig {

    @Value("${hz.ip:localhost}")
    private String ip;

    @Bean
    @Scope(value = "prototype")
    public HazelcastInstance hazelcastInstance() {

        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().addAddress(ip);

        return HazelcastClient.newHazelcastClient(config);
    }
}
