package com.hazelcast.aws.ecs;

import com.hazelcast.aws.AwsDiscoveryStrategyFactory;
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
        config.getNetworkConfig().getInterfaces().setEnabled(true).addInterface("10.0.*.*");
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getAwsConfig()
                  .setEnabled(true)
                  .setProperty("region", "eu-central-1")
                  .setProperty("tag-key", "aws:cloudformation:stack-name")
                  .setProperty("tag-value", "EC2ContainerService-test-cluster");
        return config;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
