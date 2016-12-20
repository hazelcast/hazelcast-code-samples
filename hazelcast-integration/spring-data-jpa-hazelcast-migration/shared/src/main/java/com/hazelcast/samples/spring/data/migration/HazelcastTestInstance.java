package com.hazelcast.samples.spring.data.migration;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.data.hazelcast.HazelcastKeyValueAdapter;
import org.springframework.data.keyvalue.core.KeyValueTemplate;

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

    /**
     * A {@link org.springframework.data.keyvalue.core.KeyValueTemplate KeyValueTemplate}
     * instructs Spring to use Hazelcast for key-value repositories.
     *
     * @param hazelcastInstance Created above
     * @return The template from which to build repository operations
     */
    @Bean
    public KeyValueTemplate keyValueTemplate(HazelcastInstance hazelcastInstance) {
        return new KeyValueTemplate(new HazelcastKeyValueAdapter(hazelcastInstance));
    }
}
