package com.hazelcast.samples.eureka;

import com.hazelcast.config.Config;
import com.hazelcast.eureka.one.EurekaOneDiscoveryStrategyFactory;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.shared.transport.jersey3.Jersey3TransportClientFactories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

@SpringBootApplication
public class Application {

    @Value("${hazelcast.port:5701}")
    private int hazelcastPort;

    @Bean
    public Config hazelcastConfig() {
        // The plugin can reuse a client supplied by the application. Use the Jakarta Jersey transport.
        String namespace = "hazelcast";
        String instanceId = UUID.randomUUID().toString();
        MyDataCenterInstanceConfig instanceConfig = new MyDataCenterInstanceConfig(namespace) {
            @Override
            public String getInstanceId() {
                return instanceId;
            }
        };
        DiscoveryClient client = new DiscoveryClient(new ApplicationInfoManager(instanceConfig),
                new DefaultEurekaClientConfig(namespace), Jersey3TransportClientFactories.getInstance());
        EurekaOneDiscoveryStrategyFactory.setEurekaClient(client);

        Config config = new Config();
        config.getNetworkConfig().setPort(hazelcastPort);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getEurekaConfig()
              .setEnabled(true)
              .setProperty("self-registration", "true")
              .setProperty("namespace", namespace)
              .setProperty("use-metadata-for-host-and-port", "true");
        return config;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
