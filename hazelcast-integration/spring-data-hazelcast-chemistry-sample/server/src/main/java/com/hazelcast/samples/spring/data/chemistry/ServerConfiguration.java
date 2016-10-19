package com.hazelcast.samples.spring.data.chemistry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * <P>Spring beans specific to a Hazelcast server-side process.
 * </P>
 * <P>
 * To keep the example simple, the config is done with the
 * file {@code hazelcast.xml}. This XML allows for multiple
 * Hazelcast server instances on the same host to find each
 * other, starting from port 5701 and upwards.
 * </P>
 */
@Configuration
public class ServerConfiguration {

	/**
	 * <P>Create a Hazelcast server as a Spring bean.
	 * </P>
	 * 
	 * @return A Hazelcast server singleton
	 */
	@Bean
	public HazelcastInstance hazelcastInstance() {
		Config config = new ClasspathXmlConfig("hazelcast.xml");
		
		return Hazelcast.newHazelcastInstance(config);
	}
	
}
