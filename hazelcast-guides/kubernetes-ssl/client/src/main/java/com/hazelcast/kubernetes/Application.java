package com.hazelcast.kubernetes;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.SSLConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    private static Config config;

    @Bean
    public ClientConfig hazelcastConfig() {
        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().addAddress("hz-hazelcast-enterprise");
        config.getNetworkConfig().setSSLConfig(new SSLConfig()
                .setProperty("trustStore", "truststore")
                .setProperty("trustStorePassword", "123456")
                .setEnabled(true));
        return config;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
