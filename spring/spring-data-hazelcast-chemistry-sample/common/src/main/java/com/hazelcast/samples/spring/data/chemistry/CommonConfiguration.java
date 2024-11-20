package com.hazelcast.samples.spring.data.chemistry;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.hazelcast.repository.config.EnableHazelcastRepositories;

/**
 * Spring beans connecting {@code spring-data-keyvalue} to the
 * specific implementation of the underlying Key-Value store.
 */
@Configuration
@EnableHazelcastRepositories(basePackages = {"com.hazelcast.samples.spring.data.chemistry"})
public class CommonConfiguration {
}
