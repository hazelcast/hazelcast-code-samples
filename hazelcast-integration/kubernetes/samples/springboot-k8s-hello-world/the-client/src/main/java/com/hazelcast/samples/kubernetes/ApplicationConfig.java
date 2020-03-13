package com.hazelcast.samples.kubernetes;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory;
import com.hazelcast.kubernetes.KubernetesProperties;
import com.hazelcast.spi.properties.ClusterProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;


/**
 * <p>
 * Dynamic configuration for the Hazelcast client. The logic is
 * the same as for the Hazelcast server.
 * </p>
 * <p>
 * Depending on an environment variable "{@code k8s}", modify the
 * configuration loaded from "{@code hazelcast-client.xml}" before
 * returning the Java object that Spring will use to create the
 * Hazelcast client instance.
 * </p>
 * <p>
 * <i>If</i> we are not in Kubernetes, direct the client that the
 * server can be found at "{@code 127.0.0.1:5701}", as this is
 * how the server will try to exist.
 * </p>
 * <p>
 * <i>If</i> we are in Kubernetes, there are two steps.
 * </p>
* <ol>
 * <li>Configure Kubenetes Plugin
 * <p>Create an instance of the Kubernetes discovery plugin, and configure it.</p>
 * <p>Configuration here means to provide the tag string for Hazelcast <i>servers</i>.
 * The client needs to find a server to connect to, clients don't connect to
 * anything else.
 * </li>
 * <li>Use Kubenetes Plugin
 * <p>Tell the Hazelcast client to use the Kubernetes discovery plugin set
 * up in step 1.</p>
 * </li>
 * </ol>
 * <p>As a bonus, in the "{@code hazelcast-client.xml}" file, the property
 * "{@code hazelcast.client.statistics.enabled}" is set to true, so
 * statistics about this Hazelcast client are sent via the Hazelcast servers
 * to the Hazelcast Management Center.
 * </p>
 */
@Configuration
public class ApplicationConfig {

    private static final String DEFAULT_FALSE = "false";
    private static final String HAZELCAST_SERVICE_NAME = "service-hazelcast-server.default.svc.cluster.local";

    @Bean
    public ClientConfig clientConfig() throws Exception {
        ClientConfig clientConfig =
                new XmlClientConfigBuilder("hazelcast-client.xml").build();

        boolean k8s = System.getProperty("k8s", DEFAULT_FALSE).equalsIgnoreCase("true");

        if (k8s) {
            // Step (1) in docs above
            HazelcastKubernetesDiscoveryStrategyFactory hazelcastKubernetesDiscoveryStrategyFactory
                = new HazelcastKubernetesDiscoveryStrategyFactory();
            DiscoveryStrategyConfig discoveryStrategyConfig =
                    new DiscoveryStrategyConfig(hazelcastKubernetesDiscoveryStrategyFactory);
            discoveryStrategyConfig.addProperty(KubernetesProperties.SERVICE_DNS.key(),
                    HAZELCAST_SERVICE_NAME);

            // Step (2) in docs above
            clientConfig.setProperty(ClusterProperty.DISCOVERY_SPI_ENABLED.toString(), "true");
            clientConfig
                .getNetworkConfig()
                .getDiscoveryConfig()
                .addDiscoveryStrategyConfig(discoveryStrategyConfig);
        } else {
            clientConfig
                .getNetworkConfig()
                .setAddresses(Collections.singletonList("127.0.0.1:5701"));
        }

        return clientConfig;
    }
}
