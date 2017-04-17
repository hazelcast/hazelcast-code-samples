package com.hazelcast.samples.eureka.partition.groups;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hazelcast.spi.discovery.integration.DiscoveryService;
import com.hazelcast.spi.discovery.integration.DiscoveryServiceProvider;
import com.hazelcast.spi.discovery.integration.DiscoveryServiceSettings;

/**
 * <P>Provides a discovery service :-)
 * </P>
 * <P>In this case, use auto-wiring to provide the instance that
 * Spring has enriched, rather than create one anew.
 * </P>
 */
@Component
public class MyDiscoveryServiceProvider implements DiscoveryServiceProvider {

	@Autowired
	private MyEurekaDiscoveryService myEurekaDiscoveryService;


	/**
	 * <P>Return the {@link MyEurekaDiscoveryService} singleton.
	 * </P>
	 * 
	 * @param not_used Ignored as we only return the existing object.
	 * @return The object {@code @Autowired} by Spring
	 */
	@Override
	public DiscoveryService newDiscoveryService(DiscoveryServiceSettings not_used) {
		return this.myEurekaDiscoveryService;
	}

}
