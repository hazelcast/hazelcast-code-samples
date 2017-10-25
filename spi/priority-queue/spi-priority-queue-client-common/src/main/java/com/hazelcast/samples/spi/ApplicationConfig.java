package com.hazelcast.samples.spi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ProxyFactoryConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;

/**
 * <p>Client configuration, create Hazelcast client
 * as a Spring {@code @Bean}.
 * </p>
 */
@Configuration
public class ApplicationConfig {

	/**
	 * <p>Create a client configuration from the
	 * "{@code hazelcast-client.xml}" file, the
	 * default the code looks for.
	 * </p>
	 * <p>Extend this with some special service
	 * configuration.
	 * </p>
	 * 
	 * @return Client configuration for later use
	 * @throws Exception File not found, etc
	 */
    @Bean
    public ClientConfig clientConfig() throws Exception {
    		// Load from "hazelcast-client.xml", the default
    		ClientConfig clientConfig = new XmlClientConfigBuilder().build();
    				
    		// Add the ability to access our remote service
    		ProxyFactoryConfig proxyFactoryConfig = new ProxyFactoryConfig();
    		proxyFactoryConfig.setClassName(MyPriorityQueueProxyFactory.class.getName());
    		proxyFactoryConfig.setService(MyPriorityQueue.SERVICE_NAME);
    		
    		clientConfig.getProxyFactoryConfigs().add(proxyFactoryConfig);
    	
    		return clientConfig;
    }

    /**
     * <p>Create a Hazelcast client using the supplied
     * configuration.
     * </p>
     *
     * @param clientConfig The {@code @Bean} created above
     * @return A Hazelcast client
     */
    @Bean
    public HazelcastInstance hazelcastInstance(ClientConfig clientConfig) {
            return HazelcastClient.newHazelcastClient(clientConfig);
    }

}
