package com.hazelcast.samples.spring.data.migration;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;

/**
 * Test support for Hazelcast repositories, as these are not yet built on demand by Spring Boot.
 *
 * <u><b>MIGRATION PATH</b></u>
 * <ol>
 * <li>Add this test helper class.</li>
 * </ol>
 */
public class HazelcastTestInstance {

    /**
     * Create a Hazelcast instance for testing, a server instance not connected to others, so turn off discovery.
     *
     * @return A standalone server instance, auto-closeable
     */
    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();

        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

        return Hazelcast.newHazelcastInstance(config);
    }
}
