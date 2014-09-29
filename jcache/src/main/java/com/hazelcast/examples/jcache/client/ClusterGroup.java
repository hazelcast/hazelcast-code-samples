package com.hazelcast.examples.jcache.client;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.examples.AbstractApp;

/**
 * Server cluster required for the clients to connect
 */
public class ClusterGroup extends AbstractApp {

    public static void main(String[] args) throws InterruptedException {
        ClusterGroup app = new ClusterGroup();
        app.runApp();
    }

    /**
     * This will setup two separate hz node instance for clients to join
     */
    public void init() {
        Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().setPort(5701);
        config.getGroupConfig().setName("cluster1");
        config.getGroupConfig().setPassword("cluster1pass");

        final HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(config);

        Config config2 = new Config();
        config2.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config2.getNetworkConfig().setPort(5702);
        config2.getGroupConfig().setName("cluster2");
        config2.getGroupConfig().setPassword("cluster2pass");

        final HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config2);
    }



    public void runApp()
            throws InterruptedException {
        init();
    }

    public void shutdown() {
        Hazelcast.shutdownAll();
    }
}
