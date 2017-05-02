package com.hazelcast.samples.eureka.partition.groups;

import com.hazelcast.spi.discovery.integration.DiscoveryService;
import com.hazelcast.spi.discovery.integration.DiscoveryServiceProvider;
import com.hazelcast.spi.discovery.integration.DiscoveryServiceSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides a discovery service :-)
 * <p>
 * In this case, use auto-wiring to provide the instance that
 * Spring has enriched, rather than create one anew.
 */
@Component
public class MyDiscoveryServiceProvider implements DiscoveryServiceProvider {

    @Autowired
    private MyEurekaDiscoveryService myEurekaDiscoveryService;

    /**
     * Return the {@link MyEurekaDiscoveryService} singleton.
     *
     * @param unused Ignored as we only return the existing object.
     * @return The object {@code @Autowired} by Spring
     */
    @Override
    public DiscoveryService newDiscoveryService(DiscoveryServiceSettings unused) {
        return myEurekaDiscoveryService;
    }
}
