package com.hazelcast.samples.kubernetes;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory;
import com.hazelcast.kubernetes.KubernetesProperties;
import com.hazelcast.spi.properties.ClusterProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * <p>
 * Dynamic configuration for the Hazelcast server. It will set itself up
 * for Kubernetes or not depending on the environment variable "{@code k8s}".
 * </p>
 * <p>
 * So how this class works is it makes the Hazelcast configuration from
 * the "{@code hazelcast.xml}" file, amends it somehow, and returns it
 * to Spring to use to build a Hazelcast server instance.
 * </p>
 * <p>
 * <i>If</i> we are not in Kubernetes, amend the configuration loaded from
 * "{@code hazelcast.xml}" to set discovery to use "{@code 127.0.0.1:5701}".
 * </p>
 * <p>
 * <i>If</i> we are in Kubernetes, it's a fraction more complicated, but
 * still not exactly difficult.
 * </p>
 * <ol>
 * <li>Configure Kubenetes Plugin
 * <p>Create an instance of the Kubernetes discovery plugin, and configure it.</p>
 * <p>This plugin connects to Kubernetes to retrieve the location of existing
 * Hazelcast servers. We won't know their locations in advance, so can't specify
 * it in "{@code hazelcast.xml}" so have to look them up at run time.
 * </p>
 * <p>As this process is a Hazelcast server, it will also register it's location
 * with Kubernetes so that the next Hazelcast server to start will find it.
 * </p>
 * <p>The registration tag is "{@code service-hazelcast-server}", and this
 * distringuishes it from other services in Kubernetes such as the Hazelcast
 * Management Center.</p>
 * </li>
 * <li>Use Kubenetes Plugin
 * <p>Tell the Hazelcast server to use the Kubernetes discovery plugin set
 * up in step 1.</p>
 * </li>
 * </ol>
 */
@Configuration
public class ApplicationConfig {

    private static final String DEFAULT_FALSE = "false";
    private static final String HAZELCAST_SERVICE_NAME = "service-hazelcast-server.default.svc.cluster.local";
    private static final String MANCENTER_SERVICE_NAME = "service-hazelcast-management-center";

    @Bean
    public Config config() {
        Config config = new ClasspathXmlConfig("hazelcast.xml");

        boolean k8s = System.getProperty("k8s", DEFAULT_FALSE).equalsIgnoreCase("true");

        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);

        if (k8s) {
            // Step (1) in docs above
            HazelcastKubernetesDiscoveryStrategyFactory hazelcastKubernetesDiscoveryStrategyFactory
                = new HazelcastKubernetesDiscoveryStrategyFactory();
            DiscoveryStrategyConfig discoveryStrategyConfig =
                    new DiscoveryStrategyConfig(hazelcastKubernetesDiscoveryStrategyFactory);
            discoveryStrategyConfig.addProperty(KubernetesProperties.SERVICE_DNS.key(),
                    HAZELCAST_SERVICE_NAME);

            // Step (2) in docs above
            config.setProperty(ClusterProperty.DISCOVERY_SPI_ENABLED.toString(), "true");
            joinConfig.getDiscoveryConfig().addDiscoveryStrategyConfig(discoveryStrategyConfig);
        } else {
            joinConfig.getTcpIpConfig().setEnabled(true).setMembers(Collections.singletonList("127.0.0.1:5701"));
        }

        return config;
    }
}
