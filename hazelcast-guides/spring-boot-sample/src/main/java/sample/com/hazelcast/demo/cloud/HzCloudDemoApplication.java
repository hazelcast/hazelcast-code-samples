package sample.com.hazelcast.demo.cloud;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.SSLConfig;

// tag::class[]
@SpringBootApplication
public class HzCloudDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(HzCloudDemoApplication.class, args);
    }

    @ConditionalOnProperty(
        name = "hazelcast.cloud.tlsEnabled",
        havingValue = "true"
    )
    @Bean
    ClientConfig hazelcastClientConfig(
        @Value("${hazelcast.cloud.discoveryToken}") String discoveryToken,
        @Value("${hazelcast.cloud.clusterId}") String clusterId,
        @Value("${hazelcast.cloud.keyStore}") Resource keyStore,
        @Value("${hazelcast.cloud.keyStorePassword}") String keyStorePassword,
        @Value("${hazelcast.cloud.trustStore}") Resource trustStore,
        @Value("${hazelcast.cloud.trustStorePassword}") String trustStorePassword
    ) throws IOException {
        Properties props = new Properties();
        props.setProperty("javax.net.ssl.keyStore", keyStore.getURI().getPath());
        props.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
        props.setProperty("javax.net.ssl.trustStore", trustStore.getURI().getPath());
        props.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().setRedoOperation(true);
        config.getNetworkConfig().setSSLConfig(new SSLConfig().setEnabled(true).setProperties(props));
        config.getNetworkConfig()
            .getCloudConfig()
                .setDiscoveryToken(discoveryToken)
                .setEnabled(true);
        config.setClusterId(clusterId);
        config.setProperty("hazelcast.client.cloud.url", "https://api.cloud.hazelcast.com");

        return config;
    }

}
// end::class[]
