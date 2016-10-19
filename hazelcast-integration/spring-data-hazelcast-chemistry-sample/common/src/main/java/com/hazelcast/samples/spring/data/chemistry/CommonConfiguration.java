package com.hazelcast.samples.spring.data.chemistry;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hazelcast.HazelcastKeyValueAdapter;
import org.springframework.data.hazelcast.repository.config.EnableHazelcastRepositories;
import org.springframework.data.keyvalue.core.KeyValueTemplate;

/**
 * Spring beans connecting {@code spring-data-keyvalue} to the
 * specific implementation of the underlying Key-Value store.
 */
@Configuration
@EnableHazelcastRepositories(basePackages = {"com.hazelcast.samples.spring.data.chemistry"})
public class CommonConfiguration {

    /**
     * A {@link org.springframework.data.keyvalue.core.KeyValueAdapter KeyValueAdapter} bean
     * provides the bridge between the exposed Spring Key-Value operations and the implementation
     * provider.
     *
     * @param hazelcastInstance A client or a server
     * @return A connection to a Hazelcast instance
     */
    @Bean
    public HazelcastKeyValueAdapter hazelcastKeyValueAdapter(HazelcastInstance hazelcastInstance) {
        return new HazelcastKeyValueAdapter(hazelcastInstance);
    }

    /**
     * The template for key value operations needs instantiated with
     * the implementation.
     *
     * The implementation of {@code spring-data-*} must provide the
     * {@code keyValueAdapter} bean.
     *
     * @param hazelcastKeyValueAdapter The above bean
     * @return A template for key value operations
     */
    @Bean
    public KeyValueTemplate keyValueTemplate(HazelcastKeyValueAdapter hazelcastKeyValueAdapter) {
        return new KeyValueTemplate(hazelcastKeyValueAdapter);
    }
}
