package com.hazelcast.wan;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.map.IMap;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The passive side of the WAN configuration. This main class starts the
 * cluster member receiving and applying the map update events replicated
 * from the active side started with {@link WanSource}.
 */
public class WanTarget {

    /*
    The Hazelcast Enterprise license key. This needs to be set in order
    to run the sample.
     */
    private static final String LICENSE_KEY = "YOUR_LICENSE_KEY";

    public static void main(String[] args) {
        //        HazelcastInstance hz = instanceConfiguredWithXml();
        //        HazelcastInstance hz = instanceConfiguredWithYaml();
        HazelcastInstance hz = instanceConfiguredProgrammatically();

        ILogger logger = hz.getLoggingService().getLogger(WanTarget.class);

        IMap<Object, Object> map = hz.getMap("wan-replicated-map");
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(() ->
                logger.info("Size of " + map.getName() + ": " + map.size()), 0, 1, TimeUnit.SECONDS);
    }

    private static HazelcastInstance instanceConfiguredWithXml() {
        System.setProperty("hazelcast.config", "classpath:hazelcast-wan-target.xml");
        return Hazelcast.newHazelcastInstance();
    }

    private static HazelcastInstance instanceConfiguredWithYaml() {
        System.setProperty("hazelcast.config", "classpath:hazelcast-wan-target.yaml");
        return Hazelcast.newHazelcastInstance();
    }

    private static HazelcastInstance instanceConfiguredProgrammatically() {
        Config config = new Config();

        /*
        The name of the target cluster. This needs to be set in the `wan-publisher`
        configuration in the WAN source cluster.
         */
        config.setClusterName("wan-target");
        config.setLicenseKey(LICENSE_KEY);
        /*
        The port the WAN target member to listen on. This port needs to
        be set in the WAN source cluster's `wan-publisher` configuration.
         */
        config.getNetworkConfig().setPort(6000);

        return Hazelcast.newHazelcastInstance(config);
    }
}
