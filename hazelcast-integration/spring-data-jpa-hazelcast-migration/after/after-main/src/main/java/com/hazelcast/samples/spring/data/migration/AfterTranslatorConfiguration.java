package com.hazelcast.samples.spring.data.migration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hazelcast.HazelcastKeyValueAdapter;
import org.springframework.data.hazelcast.repository.config.EnableHazelcastRepositories;
import org.springframework.data.keyvalue.core.KeyValueTemplate;

/**
 * Configuration class to make the necessary Spring beans available for this to work.
 *
 * Only two needed, a Hazelcast instance as an implementation for the <i>key-value</i> repositories,
 * and to tell Spring to use that Hazelcast instance (which ideal Spring could figure out).
 *
 * Here the Hazelcast instance is a client, meaning the data is hosted elsewhere
 * on one or more Hazelcast server instances. You can still access it from here,
 * and you don't need to care that it is hosted elsewhere so long as it is available.
 *
 * <u><b>MIGRATION PATH</b></u>
 * <ol>
 * <li>Add a method to connect this JVM to the Hazelcast cluster, and Spring to Hazelcast for <i>key-value</i> operations.</li>
 * <li>Remove the JPA configurution, which moves to {@link HazelcastServerConfiguration}.</li>
 * </ol>
 */
@Configuration
@EnableHazelcastRepositories
public class AfterTranslatorConfiguration {

    /**
     * Create a Hazelcast client, to make the data hosted on the Hazelcast servers available as if locally held.
     *
     * This is boilerplate coding, one day will likely be auto-configured.
     *
     * @return A Hazelcast instance
     * @throws Exception Unlikely, but if file not found etc
     */
    @Bean
    public HazelcastInstance hazelcastInstance() throws Exception {
        ClientConfig clientConfig = new XmlClientConfigBuilder("hazelcast-client.xml").build();
        return HazelcastClient.newHazelcastClient(clientConfig);
    }

    /**
     * A {@link org.springframework.data.keyvalue.core.KeyValueTemplate KeyValueTemplate}
     * instructs Spring to use Hazelcast for key-value repositories.
     *
     * This is boilerplate coding, one day will likely be auto-configured.
     *
     * @param hazelcastInstance Created above
     * @return The template from which to build repository operations
     */
    @Bean
    public KeyValueTemplate keyValueTemplate(HazelcastInstance hazelcastInstance) {
        return new KeyValueTemplate(new HazelcastKeyValueAdapter(hazelcastInstance));
    }
}
