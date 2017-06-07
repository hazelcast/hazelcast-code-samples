package com.hazelcast.ocp.configuration;


import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;

import java.io.InputStream;

@Configuration
@Profile("production")
@Slf4j
public class KubernetesConfig {

    @Bean
    @Scope(value = "prototype")
    public HazelcastInstance hazelcastInstance(){
        return HazelcastClient.newHazelcastClient();
    }
}
