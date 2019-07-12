package com.hazelcast.kubernetes;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    private static Config config;

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getKubernetesConfig().setEnabled(true);
        return config;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
