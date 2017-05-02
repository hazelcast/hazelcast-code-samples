package com.hazelcast.samples.eureka.partition.groups;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.spi.discovery.integration.DiscoveryServiceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.hazelcast.config.PartitionGroupConfig.MemberGroupType.ZONE_AWARE;

/**
 * Creates a Hazelcast configuration object, which Spring Boot
 * will use to create a Hazelcast instance based on that configuration.
 */
@Configuration
public class MyConfiguration {

    /**
     * Create a Hazelcast configuration object for a server that differs from
     * the default in four ways.
     * <ol>
     * <li><b>Name</b>
     * Use {@link Constants} to give the cluster a name,
     * here more for logging and diagnostics than to avoid inadvertant
     * connections.
     * </li>
     * <li><b>Networking</b>
     * Turn off the default multicast broadcasting mechanism for servers
     * to find each other, in favor of a plug-in that will somehow provide
     * the locations of the servers. The "<em>somehow</em>" being to find
     * their locations in {@code Eureka}.
     * </li>
     * <li><b>Partition Groups</b>
     * Activate {@code ZONE_AWARE} partitioning, where we guide Hazelcast
     * in where to place data master and data backup copies with external
     * meta-data (which we get from {@code Eureka}.
     * </li>
     * <li><b>Map Config</b>
     * Configure maps for safety (ie. have backups) or where safety isn't
     * required (ie. have no backups). Backups get placed in a different zone
     * from the original.
     * </ol>
     *
     * @param discoveryServiceProvider A {@link MyDiscoveryServiceProvider} instance.
     * @return Configuration for a Hazelcast server.
     */
    @Bean
    public Config config(DiscoveryServiceProvider discoveryServiceProvider) {

        Config config = new Config();

        // Naming
        config.getGroupConfig().setName(Constants.CLUSTER_NAME);

        // Discovery
        config.setProperty("hazelcast.discovery.enabled", Boolean.TRUE.toString());
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig
                .getMulticastConfig()
                .setEnabled(false);
        joinConfig
                .getDiscoveryConfig()
                .setDiscoveryServiceProvider(discoveryServiceProvider);

        // Partition Groups
        config.getPartitionGroupConfig()
                .setEnabled(true)
                .setGroupType(ZONE_AWARE);

        // Maps
        config.getMapConfigs()
                .put(Constants.MAP_NAME_SAFE, new MapConfig(Constants.MAP_NAME_SAFE).setBackupCount(1));
        config.getMapConfigs()
                .put(Constants.MAP_NAME_UNSAFE, new MapConfig(Constants.MAP_NAME_UNSAFE).setBackupCount(0));

        return config;
    }
}
