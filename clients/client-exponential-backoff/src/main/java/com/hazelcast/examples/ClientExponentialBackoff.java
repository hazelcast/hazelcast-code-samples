package com.hazelcast.examples;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ConnectionRetryConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class ClientExponentialBackoff {

    public static void main(String[] args) throws InterruptedException {
        HazelcastInstance instance = Hazelcast.newHazelcastInstance();

        ClientConfig clientConfig = new ClientConfig();
        ConnectionRetryConfig connectionRetryConfig = clientConfig.getConnectionStrategyConfig().getConnectionRetryConfig();

        connectionRetryConfig
                .setFailOnMaxBackoff(false)
                .setInitialBackoffMillis(1000)
                .setMaxBackoffMillis(60000)
                .setMultiplier(2)
                .setJitter(0.2);

        HazelcastClient.newHazelcastClient(clientConfig);


        /*
         * We are killing the cluster and waiting for some time to restart it.
         * Client will try to reconnect according to retry config with increasing backoff times
         * and will connect to the new cluster.
         */
        instance.shutdown();
        Thread.sleep(10000);
        Hazelcast.newHazelcastInstance();

    }
}
