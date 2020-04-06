package com.hazelcast.samples;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.util.ClientStateListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class ClientStrategy {

    public static void main(String[] args) {
        HazelcastInstance server = Hazelcast.newHazelcastInstance();

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getConnectionStrategyConfig().setAsyncStart(true);
        ClientStateListener clientStateListener = new ClientStateListener(clientConfig);
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        boolean started = false;
        try {
            started = clientStateListener.awaitConnected();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (started) {
            //client started and ready to operate.
            System.out.println("Is Client connected to cluster: " + clientStateListener.isConnected());
        }

        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }
}
