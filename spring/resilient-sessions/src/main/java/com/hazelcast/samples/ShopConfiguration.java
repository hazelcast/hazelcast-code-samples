package com.hazelcast.samples;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientFailoverConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.samples.model.Basket;
import com.hazelcast.samples.model.BasketItem;
import com.hazelcast.samples.model.ProductDto;
import com.hazelcast.spring.HazelcastObjectExtractionConfiguration;
import com.hazelcast.spring.session.SessionMapCustomizer;
import com.hazelcast.spring.session.config.annotation.SpringSessionHazelcastInstance;
import com.hazelcast.spring.session.config.annotation.web.http.EnableHazelcastHttpSession;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.FlushMode;

import java.util.List;

import static com.hazelcast.samples.ShopConfiguration.SESSION_MAP_NAME;
import static com.hazelcast.spring.session.HazelcastSessionConfiguration.applySerializationConfig;

@EnableAutoConfiguration(exclude = {HazelcastObjectExtractionConfiguration.class})
@Configuration
@EnableHazelcastHttpSession(sessionMapName = SESSION_MAP_NAME, flushMode = FlushMode.IMMEDIATE)
public class ShopConfiguration {

    public static final String SESSION_MAP_NAME = "shopSessions";

    /**
     * Note: not using Config as a bean to avoid problem with default autoconfiguration.
     */
    @Bean
    @SpringSessionHazelcastInstance
    @ConditionalOnExpression("${use.wan.replication:false} == false")
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        config.getJetConfig().setEnabled(true);
        config.getSerializationConfig().getCompactSerializationConfig()
              .addSerializer(new BasketItem.Serializer())
              .addSerializer(new Basket.Serializer())
              .addSerializer(new ProductDto.Serializer());

        // Note: map configuration should be done via SessionRepositoryCustomizer!

        config.addMapConfig(new MapConfig("products"));
        applySerializationConfig(config);
        return Hazelcast.newHazelcastInstance(config);
    }
    /**
     * Note: not using Config as a bean to avoid problem with default autoconfiguration.
     */
    @Bean
    @SpringSessionHazelcastInstance
    @ConditionalOnExpression("${use.wan.replication:false} == true")
    public HazelcastInstance hazelcastClient() {
        ClientConfig config = new ClientConfig();
        BasketItem.Serializer basketItemSerializer = new BasketItem.Serializer();
        Basket.Serializer basketSerializer = new Basket.Serializer();
        ProductDto.Serializer productDtoSerializer = new ProductDto.Serializer();
        config.getSerializationConfig().getCompactSerializationConfig()
              .addSerializer(basketItemSerializer)
              .addSerializer(basketSerializer)
              .addSerializer(productDtoSerializer);
        applySerializationConfig(config);
        config.getNetworkConfig().setAddresses(List.of("localhost:5702"));
        config.setClusterName("nyc-cluster");

        ClientConfig config2 = new ClientConfig();
        config2.getSerializationConfig().getCompactSerializationConfig()
              .addSerializer(basketItemSerializer)
              .addSerializer(basketSerializer)
              .addSerializer(productDtoSerializer);
        applySerializationConfig(config2);
        config2.getNetworkConfig().setAddresses(List.of("localhost:5701"));
        config2.setClusterName("london-cluster");

        // Note: map configuration should be done via SessionRepositoryCustomizer!
        return HazelcastClient.newHazelcastFailoverClient(new ClientFailoverConfig()
                                                                  .addClientConfig(config2)
                                                                  .addClientConfig(config)
                                                                  .setTryCount(10));
    }

    @Bean
    @ConditionalOnExpression("${use.wan.replication:false} == false")
    public SessionMapCustomizer customizeRepository() {
        return mapConfig -> {
                mapConfig.setBackupCount(1);
        };
    }

    @Bean
    public IMap<Long, ProductDto> products(HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getMap("products");
    }

}
