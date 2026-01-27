package com.hazelcast.guide.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.session.HazelcastIndexedSessionRepository;
import com.hazelcast.spring.session.HazelcastSessionConfiguration;
import com.hazelcast.spring.session.config.annotation.SpringSessionHazelcastInstance;
import com.hazelcast.spring.session.config.annotation.web.http.EnableHazelcastHttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.FlushMode;
import org.springframework.session.SaveMode;
import org.springframework.session.config.SessionRepositoryCustomizer;

import java.time.Duration;

@Configuration
@EnableHazelcastHttpSession
class SessionConfiguration {

    private static final String SESSIONS_MAP_NAME = "spring-session-map-name";

    @Bean
    public SessionRepositoryCustomizer<HazelcastIndexedSessionRepository> customize() {
        return (repository) -> {
            repository.setFlushMode(FlushMode.IMMEDIATE);
            repository.setSaveMode(SaveMode.ALWAYS);
            repository.setSessionMapName(SESSIONS_MAP_NAME);
            repository.setDefaultMaxInactiveInterval(Duration.ofSeconds(900));
        };
    }

    @Bean
    @SpringSessionHazelcastInstance
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        config.setClusterName("spring-session-cluster");

        NetworkConfig networkConf = config.getNetworkConfig();
        networkConf.getJoin().getTcpIpConfig().setEnabled(true).addMember("127.0.0.1");

        return Hazelcast.newHazelcastInstance(HazelcastSessionConfiguration.applySerializationConfig(config));
    }

}
