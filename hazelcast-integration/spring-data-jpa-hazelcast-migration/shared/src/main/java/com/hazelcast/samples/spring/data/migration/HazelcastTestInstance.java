package com.hazelcast.samples.spring.data.migration;

import org.springframework.context.annotation.Bean;
import org.springframework.data.hazelcast.HazelcastKeyValueAdapter;
import org.springframework.data.keyvalue.core.KeyValueTemplate;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * <P>Test support for Hazelcast repositories, as these are not yet
 * built on demand by Spring Boot.
 * </P>
 * <P><U><B>MIGRATION PATH</B></U></P>
 * <OL>
 * <LI>Add this test helper class.
 * </LI>
 * </OL>
 */
public class HazelcastTestInstance {

	/**
	 * <P>Create a Hazelcast instance for testing, a server
	 * instance not connected to others, so turn off discovery.
	 * </P>
	 * 
	 * @return A standalone server instance, auto-closeable
	 */
	@Bean
	public HazelcastInstance hazelcastInstance() {
		Config config = new Config();
		
		config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
		
		return Hazelcast.newHazelcastInstance(config);
	}
	
	/**
	 * <P>A {@link org.springframework.data.keyvalue.core.KeyValueTemplate KeyValueTemplate} 
	 * instructs Spring to use Hazelcast for key-value repositories.
	 * </P>
	 * 
	 * @param hazelcastInstance Created above
	 * @return The template from which to build repository operations 
	 */
	@Bean
	public KeyValueTemplate keyValueTemplate(HazelcastInstance hazelcastInstance) {
		return new KeyValueTemplate(new HazelcastKeyValueAdapter(hazelcastInstance));
	}
	
}
