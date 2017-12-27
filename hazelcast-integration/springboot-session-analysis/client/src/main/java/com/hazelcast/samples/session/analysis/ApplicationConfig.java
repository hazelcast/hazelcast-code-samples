package com.hazelcast.samples.session.analysis;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.web.WebFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Spring beans to make a Hazelcast client for HTTP session storage
 * in Hazelcast IMDG server.
 * </p>
 */
@Configuration
@Slf4j
public class ApplicationConfig {

    /**
     * <p>Configuratation properties to connect HTTP session storage
     * to the Hazelcast IMDG server.
     * </p>
     * <p>Meaning of each:
     * <ul>
     * <li><b>use-client</b> Are we a Hazecastl IMDG client or server
     * </li>
     * <li><b>client-config-location</b> How to configure the client
     * </li>
     * <li><b>map-name</b> Where to save the sessions
     * </li>
     * <li><b>sticky-session</b> Can we assume that all requests from the same
     * user will come to this process, and make optimisations on that basis.
     * </li>
     * </ul>
     * </p>
     *
     * @return Properties
     */
    @Bean
    public WebFilter webFilter() {
            Properties properties = new Properties();
            properties.put("use-client", "true");
            properties.put("client-config-location", "hazelcast-client.xml");
            properties.put("map-name", Constants.IMAP_NAME_JSESSIONID);
            properties.put("sticky-session", "false");

            log.info("Web properties {}" , properties);

            return new WebFilter(properties);
    }

    /**
     * <p>Web modules will create a Hazelcast client, turn it into
     * a Spring {@code @Bean}. Inject the {@link #WebFilter} to
     * ensure the client is created before we try to find it.
     * </p>
     *
     * @param webFilter Spring {@code @Bean} used for sequencing
     * @return Turn the client into a bean
     */
    @Bean
    public HazelcastInstance hazelcastInstance(WebFilter webFilter) {
            for (HazelcastInstance hazelcastInstance : HazelcastClient.getAllHazelcastClients()) {
                return hazelcastInstance;
        }
        return null;
    }

}
