package com.vreddy.demo.hazelcast;

import javax.management.RuntimeErrorException;

import com.hazelcast.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.hazelcast.config.MapStoreConfig.InitialLoadMode;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.properties.GroupProperty;

@Component
public class HazelcastConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastConfig.class);

    @Value("${hazelcast.publicAddress:10.93.115.214}")
    private String publicAddress;

    @Value("${hazelcast.members:10.93.115.214:9600,10.93.115.214:9601,10.93.115.214:9602}")
    private String hazelcastMembers;

    @Value("${hazelcast.port:9600}")
    private Integer hazelcastPort;

    @Value("${hazelcast.port-auto-increment:true}")
    private boolean hazelcastPortAutoIncrement;

    @Value("${hazelcast.group.name:dev}")
    private String groupName;

    @Value("${hazelcast.group.password:dev-pass}")
    private String groupPassword;

    @Value("${hazelcast.management.enabled:false}")
    private boolean managementEnabled;

    @Value("${hazelcast.management.url:}")
    private String managementUrl;

    @Value("${hazelcast.mamangement.interval:10}")
    private int updateInterval;

    @Value("${hazelcast.performance.monitoring.enabled:false}")
    private String hazelCastPerfMonitorEnabled;

    @Value("${hazelcast.performance.monitoring.delay.seconds:1800}")
    private String hazelCastPerfMonitorDelay;

    @Value("${hazelcast.back.pressure:true}")
    private String hazelBackPressureEnabled;

    @Value("${hazelcast.back.pressure.backoff.time:60000}")
    private String hazelBackPressureBackOffTimeOut;

    @Value("${hazelcast.back.pressure.sync.window:100}")
    private String hazelBackPressureSyncWindow;

    @Value("${hazelcast.operation.call.timeout.millis:60000}")
    private String hazelcastOperationCallTimeoutMillis;

    @Value("${hazelcast.query.thread_pool.size:100}")
    private int hazelcastQueryThreadPoolSize;

    @Value("${hazelcast.max.join.seconds:300}")
    private String hazelcastMaxJoinSeconds;

    @Value("${hazelcast.heartbeat.interval.seconds:1}")
    private String hazelcastHeartbeatIntervalSeconds;

    @Value("${hazelcast.max.no.heartbeat.seconds:300}")
    private String hazelcastMaxNoHeartbeatSeconds;

    @Value("${hazelcast.operation.thread.count:6}")
    private String hazelcastOperationThreadCount;

    @Value("${hazelcast.performance.monitoring.max.log.size.mb:10}")
    private String hazelCastPerfMonitorMaxLogSize;

    @Value("${hazelcast.performance.monitoring.max.log.count:10}")
    private String hazelCastPerfMonitorMaxLogCount;

    @Value("${hazelcast.initial.min.cluster.size:2}")
    private String hazelcastInitialMinClusterSize;

    @Value("${hazelcast.shutdown.hook.enabled:false}")
    private String hazelcastShutdownHookEnabled;

    @Value("${hazelcast.master.confirmation.interval.seconds:30}")
    private String hazelcastMasterConfirmationIntervalSeconds;

    @Value("${hazelcast.member.list.publish.interval.seconds:300}")
    private String hazelcastMemberListPublishIntervalSeconds;

    @Value("${hazelcast.max.no.master.confirmation.seconds:500}")
    private String hazelcastMaxNoMasterConfirmationSeconds;

    @Value("${hazelcast.atp.map.entry.max_size.per_node:20000}")
    private int goaMapMaxEntryPerNode;

    @Value("${hazelcast.partition.count:10}")
    private String partitionCount;

    @Value("${hazelcast.ttl:86400}")
    private int capacityCacheTTL;

    /**
     * method creating hazelcast instance bean.
     *
     * @param hazelcastConfig
     * @return
     */
    @Bean
    public HazelcastInstance hazelcastInstance(Config hazelcastConfig) {
        LOGGER.info("Instantiating hazelcast instance !");
        System.out.println("Hazelcast instance is up baby!");
        return Hazelcast.newHazelcastInstance(hazelcastConfig);
    }

    /**
     * method creating hazelcast config bean.
     *
     * @return
     */
    @Bean
    public Config getHazelCastConfig() {
        Config cfg = new Config();
        cfg.setProperty(GroupProperty.INITIAL_MIN_CLUSTER_SIZE.getName(), hazelcastInitialMinClusterSize);
        cfg.setProperty(GroupProperty.SHUTDOWNHOOK_ENABLED.getName(), hazelcastShutdownHookEnabled);
        cfg.setProperty(GroupProperty.MASTER_CONFIRMATION_INTERVAL_SECONDS.getName(),
                hazelcastMasterConfirmationIntervalSeconds);
        cfg.setProperty(GroupProperty.MEMBER_LIST_PUBLISH_INTERVAL_SECONDS.getName(),
                hazelcastMemberListPublishIntervalSeconds);
        cfg.setProperty(GroupProperty.MAX_NO_MASTER_CONFIRMATION_SECONDS.getName(),
                hazelcastMaxNoMasterConfirmationSeconds);
        cfg.setProperty(GroupProperty.OPERATION_CALL_TIMEOUT_MILLIS.getName(), hazelcastOperationCallTimeoutMillis);
        cfg.setProperty(GroupProperty.MAX_JOIN_SECONDS.getName(), hazelcastMaxJoinSeconds);
        cfg.setProperty(GroupProperty.HEARTBEAT_INTERVAL_SECONDS.getName(), hazelcastHeartbeatIntervalSeconds);
        cfg.setProperty(GroupProperty.MAX_NO_HEARTBEAT_SECONDS.getName(), hazelcastMaxNoHeartbeatSeconds);
        cfg.setProperty(GroupProperty.PARTITION_COUNT.getName(), partitionCount);
        NetworkConfig network = cfg.getNetworkConfig();
        network.setPort(hazelcastPort);
        network.setPortAutoIncrement(hazelcastPortAutoIncrement);
        if (publicAddress == null && publicAddress.trim().isEmpty()) {
            throw new RuntimeErrorException(new Error("Public address is null or empty"),
                    "Public address is null or empty " + publicAddress);
        }
        network.setPublicAddress(publicAddress);
        JoinConfig join = network.getJoin();
        join.getMulticastConfig().setEnabled(false);
        if (hazelcastMembers == null || hazelcastMembers.trim().isEmpty()) {
            throw new RuntimeErrorException(new Error("Hazelcast members  is null or empty"),
                    "Hazelcast members  is null or empty-" + hazelcastMembers);
        }
        join.getTcpIpConfig().addMember(hazelcastMembers).setEnabled(true);
        /* set map config */
        cfg.addMapConfig(getMapConfig("mapOne"));
        cfg.addSetConfig(getSetConfig("setOne"));
        cfg.getExecutorConfig("hz:query").setPoolSize(hazelcastQueryThreadPoolSize);
        return cfg;
    }

    private SetConfig getSetConfig(String setName) {
        SetConfig setConfig = new SetConfig();
        setConfig.setName(setName);
        setConfig.setAsyncBackupCount(0);
        setConfig.setMaxSize(1000);
        setConfig.setBackupCount(1);
        return setConfig;
    }

    /**
     * method to set mapstore configuration.
     *
     * @param mapName
     * @return
     */
    private MapConfig getMapConfig(String mapName) {
        MapConfig mapConfig = new MapConfig();
        mapConfig.setName(mapName);
        mapConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        mapConfig.setBackupCount(0);
        mapConfig.setAsyncBackupCount(1);
        mapConfig.getMaxSizeConfig().setSize(goaMapMaxEntryPerNode)
                .setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.PER_NODE);
        mapConfig.setTimeToLiveSeconds(capacityCacheTTL);
        MapStoreConfig mapStoreConfig = new MapStoreConfig();
        //mapStoreConfig.setImplementation(capacityConsumptionMapStore);
        mapStoreConfig.setWriteDelaySeconds(1);
        mapStoreConfig.setWriteBatchSize(5);
        mapStoreConfig.setWriteCoalescing(false);
        mapStoreConfig.setEnabled(Boolean.FALSE);
        mapStoreConfig.setInitialLoadMode(InitialLoadMode.LAZY);
        mapConfig.setMapStoreConfig(mapStoreConfig);
        mapConfig.setReadBackupData(true);
        return mapConfig;
    }
}