package com.hazelcast.guide.config;

import com.hazelcast.config.AttributeConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.FlushMode;
import org.springframework.session.MapSession;
import org.springframework.session.SaveMode;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.hazelcast.HazelcastIndexedSessionRepository;
import org.springframework.session.hazelcast.HazelcastSessionSerializer;
import org.springframework.session.hazelcast.PrincipalNameExtractor;
import org.springframework.session.hazelcast.config.annotation.SpringSessionHazelcastInstance;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

import java.time.Duration;

@Configuration
@EnableHazelcastHttpSession
class SessionConfiguration {

    private final String SESSIONS_MAP_NAME = "spring-session-map-name";

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

        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);

        // Add this attribute to be able to query sessions by their PRINCIPAL_NAME_ATTRIBUTE's
        AttributeConfig attributeConfig = new AttributeConfig()
                .setName(HazelcastIndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE)
                .setExtractorClassName(PrincipalNameExtractor.class.getName());

        // Configure the sessions map
        config.getMapConfig(SESSIONS_MAP_NAME)
                .addAttributeConfig(attributeConfig).addIndexConfig(
                new IndexConfig(IndexType.HASH, HazelcastIndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE));

        // Use custom serializer to de/serialize sessions faster. This is optional.
        // Note that, all members in a cluster and connected clients need to use the
        // same serializer for sessions. For instance, clients cannot use this serializer
        // where members are not configured to do so.
        SerializerConfig serializerConfig = new SerializerConfig();
        serializerConfig.setImplementation(new HazelcastSessionSerializer()).setTypeClass(MapSession.class);
        config.getSerializationConfig().addSerializerConfig(serializerConfig);

        return Hazelcast.newHazelcastInstance(config);
    }

}
