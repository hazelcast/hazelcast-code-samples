package com.hazelcast.app.common.connection;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientConnection {
    private static final Logger LOGGER = LogManager.getLogger("ClientConnection");

    private static final ClientConnection obj = new ClientConnection();

    public static ClientConnection getInstance() {
        return obj;
    }

    public HazelcastInstance connect(String instanceNameIn, String clusterNameIn, String hazelcastIpAddressIn) {
        final ClientConfig config = new ClientConfig();
        try {
            config.getNetworkConfig().addAddress(hazelcastIpAddressIn);
            config.setInstanceName(instanceNameIn);
            config.setClusterName(clusterNameIn);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info(e.getMessage());
        }
//        return HazelcastClient.newHazelcastClient(config);
        return HazelcastClient.getOrCreateHazelcastClient(config);
    }

}