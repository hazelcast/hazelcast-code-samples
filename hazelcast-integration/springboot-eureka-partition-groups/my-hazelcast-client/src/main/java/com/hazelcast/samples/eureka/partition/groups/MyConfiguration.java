package com.hazelcast.samples.eureka.partition.groups;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.discovery.integration.DiscoveryServiceProvider;

/**
 * <P>Configuration that is sufficient for Spring Boot to create a
 * Hazelcast client.
 * </P>
 * <P>Although it's all that is strictly needed, depending on the Spring Boot
 * version we might need to manually create the Hazelcast client using
 * the {@code ClientConfig} {@code @Bean}.
 * </P>
 */
@Configuration
public class MyConfiguration {

	/**
	 * <P>Create a Hazelcast configuration object for a client that differs from
	 * the default in two ways.
	 * </P>
	 * <OL>
	 * <LI><B>Name</B> 
	 * <P>Since the server has given the cluster a name to prevent accidental
	 * connections, the cluster must use this name to be able to connect.
	 * </P>
	 * </LI>
	 * <LI><B>Networking</B>
	 * <P>To find <em>some</em> servers to try to connect to, the client looks
	 * up {@code Eureka} for their addresses. Actually, Eureka here returns
	 * all the servers, but for the client we only need one to respond to
	 * establish a connection.
	 * </P>
	 * </LI>
	 * <P>Clients don't store date so don't care are about partition groups.
	 * Though the can have a local "<em>near-cache</em>" copy for even greater
	 * speed.</P>
	 * 
	 * @param discoveryServiceProvider A {@link MyDiscoveryServiceProvider} instance.
	 * @return Configuration for a Hazelcast client.
	 */
	@Bean
    public ClientConfig clientConfig(DiscoveryServiceProvider discoveryServiceProvider) {
        ClientConfig clientConfig = new ClientConfig();
		
		// Naming
		
        clientConfig.getGroupConfig().setName(Constants.CLUSTER_NAME);

		// Discovery
		
        clientConfig.setProperty("hazelcast.discovery.enabled", Boolean.TRUE.toString());
        clientConfig.getNetworkConfig().getDiscoveryConfig().setDiscoveryServiceProvider(discoveryServiceProvider);

        return clientConfig;
    }

    /**
	 * <P>Temporary coding (hopefully).
	 * </P>
	 * <P>Spring Boot deduces if a Hazelcast {@link com.hazelcast.config.Config Config} is present
	 * then a Hazelcast instance is required, but doesn't yet do this for a 
	 * {@link com.hazelcast.client.config.ClientConfig ClientConfig}.
	 * </P>
	 * <P>So help Spring Boot along, if the coding hasn't yet created a {@code HazelcastInstance}
	 * {@code @Bean}, do it for Spring Boot using the client configuration created above.
	 * </P>
	 */
    @Configuration
    @ConditionalOnMissingBean(HazelcastInstance.class)
    static class HazelcastClientConfiguration {

    	/**
    	 * <P>Create a Hazelcast instance, a client rather than
    	 * a server.
    	 * </P>
    	 * 
    	 * @param ClientConfig Config to use, client config for a client
    	 * @return The Hazelcast instance.
    	 */
        @Bean
        public HazelcastInstance hazelcastInstance(ClientConfig clientConfig) {
            return HazelcastClient.newHazelcastClient(clientConfig);
        }
    }
    
}
