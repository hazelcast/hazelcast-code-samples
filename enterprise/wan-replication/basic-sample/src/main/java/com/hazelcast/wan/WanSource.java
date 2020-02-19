package com.hazelcast.wan;

import com.hazelcast.config.Config;
import com.hazelcast.config.WanBatchPublisherConfig;
import com.hazelcast.config.WanReplicationConfig;
import com.hazelcast.config.WanReplicationRef;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.map.IMap;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The active side of the WAN configuration. This main class starts the
 * cluster member replicating to the passive side, the target cluster that
 * is started with {@link WanTarget}.
 */
public class WanSource {

    /*
    The Hazelcast Enterprise license key. This needs to be set in order
    to run the sample.
     */
    private static final String LICENSE_KEY = "YOUR_LICENSE_KEY";

    public static void main(String[] args) {
        //        HazelcastInstance hz = instanceConfiguredWithXml();
        //        HazelcastInstance hz = instanceConfiguredWithYaml();
        HazelcastInstance hz = instanceConfiguredProgrammatically();

        ILogger logger = hz.getLoggingService().getLogger(WanSource.class);
        IMap<Integer, Integer> map = hz.getMap("wan-replicated-map");

        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(() -> {
            int size = map.size();
            for (int i = size; i < size + 10; i++) {
                map.put(i, i);
            }

            logger.info("Size of " + map.getName() + ": " + map.size());
        }, 0, 1, TimeUnit.SECONDS);

    }

    private static HazelcastInstance instanceConfiguredWithXml() {
        System.setProperty("hazelcast.config", "classpath:hazelcast-wan-source.xml");
        return Hazelcast.newHazelcastInstance();
    }

    private static HazelcastInstance instanceConfiguredWithYaml() {
        System.setProperty("hazelcast.config", "classpath:hazelcast-wan-source.yaml");
        return Hazelcast.newHazelcastInstance();
    }

    private static HazelcastInstance instanceConfiguredProgrammatically() {
        Config config = new Config();

        /*
        The name of the source cluster. It doesn't matter for WAN replication.
        It is set in this sample only to make sure the WAN source and target
        cluster members don't form one cluster.
         */
        config.setClusterName("wan-source");
        config.setLicenseKey(LICENSE_KEY);

        config.getMapConfig("wan-replicated-map")
              /*
              The reference to the `wan-replication` configuration to be used for
              replicating this map. The name should match to the name set in the
              referenced `wan-replication` configuration element.
               */
              .setWanReplicationRef(new WanReplicationRef().setName("wan-sample"));

        WanReplicationConfig wanReplicationConfig = new WanReplicationConfig()
                /*
                The name of the WAN replication, to be referenced by the `name`
                attribute of the `wan-replication-ref` element of the map.
                 */
                .setName("wan-sample");

        WanBatchPublisherConfig batchPublisherConfig = new WanBatchPublisherConfig()
                /*
                The name of the cluster that WAN replication should replicate to.
                Should match to the cluster name set in the target cluster's configuration.
                 */
                .setClusterName("wan-target")
                .setTargetEndpoints("127.0.0.1:6000");

        /*
        Make sure the following two "links" are set.
         */
        wanReplicationConfig.addBatchReplicationPublisherConfig(batchPublisherConfig);
        config.addWanReplicationConfig(wanReplicationConfig);

        return Hazelcast.newHazelcastInstance(config);
    }
}
