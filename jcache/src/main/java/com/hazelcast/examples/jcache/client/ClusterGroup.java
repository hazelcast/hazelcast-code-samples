package com.hazelcast.examples.jcache.client;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.examples.AbstractApp;

/**
 * Server cluster required for the clients to connect
 */
public class ClusterGroup extends AbstractApp {

    public static void main(String[] args) {
        ClusterGroup app = new ClusterGroup();
        app.runApp();
    }

    /**
     * This will setup two separate hz node instance for clients to join
     */
    public void init() {
        Config config1 = new Config();
        config1.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config1.getNetworkConfig().setPort(5701);
        config1.setClusterName("cluster1");

        Hazelcast.newHazelcastInstance(config1);

        Config config2 = new Config();
        config2.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config2.getNetworkConfig().setPort(5702);
        config2.setClusterName("cluster2");

        Hazelcast.newHazelcastInstance(config2);
    }

    private void runApp() {
        init();
    }

    public void shutdown() {
        Hazelcast.shutdownAll();
    }
}
