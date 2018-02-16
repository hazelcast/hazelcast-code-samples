package com.hazelcast.spring.cache;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
class CachingConfiguration implements CachingConfigurer {

    @Bean
    public CacheManager cacheManager() {
        ClientConfig config = new ClientConfig();
        config.getGroupConfig().setName("grp");
        config.getGroupConfig().setPassword("grp-pass");
        config.getNetworkConfig().addAddress("127.0.0.1:5701");
        HazelcastInstance client = HazelcastClient.newHazelcastClient(config);

        return new HazelcastCacheManager(client);
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return null;
    }
}
