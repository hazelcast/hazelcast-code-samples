package com.hazelcast.samples.json.jsongrid;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.ClasspathYamlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;

/**
 * <p>Create a {@link com.hazelcast.config.Config Config} object
 * as a Spring {@code @Bean}. Spring Boot will deduce that we
 * want a Hazelcast instance and build one using this configuration.
 * </p>
 * <p>Notes:
 * </p>
 * <ol>
 * <li>
 * <p>Most of the configuration is loaded from a YAML file.
 * </p>
 * </li>
 * <li>
 * <p>Once the configuration is loaded from YAML, we extend it from
 * Java by making a Spring bean provide map loaders.
 * </p>
 * <p>If we didn't want to use Spring, we could do this from YAML too
 * with:
 * </p>
 * <pre>
 * map:
 *    default:
 *      map-store:
 *        enabled: true
 *        initial-mode: LAZY
 *        class-name: com.hazelcast.examples.DummyStore
 * </pre>
 * </li>
 * </ol>
 */
@Configuration
public class ApplicationConfig {

    /**
     * <p>Hazelcast configuration object, built from YML then
     * extended from Java.
     * </p>
     *
     * @param myMapLoaderFactory Spring {@code @Bean}
     * @return Configuration for a Hazelcast instance
     */
    @Bean
    public Config config(MyMapLoaderFactory myMapLoaderFactory) {
        Config config = new ClasspathYamlConfig("application.yml");

        MapConfig defaultMapConfig = config.getMapConfig("default");

        MapStoreConfig myMapStoreConfig = new MapStoreConfig();
        myMapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER);
        myMapStoreConfig.setFactoryImplementation(myMapLoaderFactory);

        defaultMapConfig.setMapStoreConfig(myMapStoreConfig);

        return config;
    }
}
