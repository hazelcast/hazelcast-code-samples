package com.hazelcast.samples.spring.data.chemistry;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring beans specific to a Hazelcast server-side process.
 *
 * To keep the example simple, the config is done with the
 * file {@code hazelcast.xml}. This XML allows for multiple
 * Hazelcast server instances on the same host to find each
 * other, starting from port 5701 and upwards.
 */
@Configuration
public class ServerConfiguration {

    /**
     * Create a Hazelcast server as a Spring bean.
     *
     * @return A Hazelcast server singleton
     */
    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new ClasspathXmlConfig("hazelcast.xml");

        return Hazelcast.newHazelcastInstance(config);
    }
}
