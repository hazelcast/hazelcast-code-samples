package com.hazelcast.kubernetes;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class Application {

    private static Config config;

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.getProperties().setProperty("hazelcast.discovery.enabled", "true");
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);

        HazelcastKubernetesDiscoveryStrategyFactory discoveryStrategyFactory = new HazelcastKubernetesDiscoveryStrategyFactory();
        Map<String, Comparable> properties = new HashMap<>();
        joinConfig.getDiscoveryConfig()
                  .addDiscoveryStrategyConfig(new DiscoveryStrategyConfig(discoveryStrategyFactory, properties));

        return config;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
