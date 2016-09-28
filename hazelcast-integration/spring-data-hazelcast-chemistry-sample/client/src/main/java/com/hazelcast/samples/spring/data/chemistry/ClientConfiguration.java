package com.hazelcast.samples.spring.data.chemistry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;

/**
 * <P>Spring beans specific to a Hazelcast client-side process.
 * </P>
 */
@Configuration
public class ClientConfiguration {

	/**
	 * <P>Create a Hazelcast client and wrap it as a Spring bean.
	 * </P>
	 * 
	 * @return A Hazelcast client singleton
	 */
	@Bean
	public HazelcastInstance hazelcastInstance() throws Exception {
		ClientConfig clientConfig = new XmlClientConfigBuilder("hazelcast-client.xml").build();
		
		return HazelcastClient.newHazelcastClient(clientConfig);
	}

}
