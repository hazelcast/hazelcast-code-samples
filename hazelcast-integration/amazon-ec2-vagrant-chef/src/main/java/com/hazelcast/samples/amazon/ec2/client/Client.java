package com.hazelcast.samples.amazon.ec2.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.AwsConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

/**
 * Example of a client connecting to a Hazelcast Cluster running on Amazon EC2.
 *
 * By default we have set insideAws to false, this means that you can run this client from your desktop and it will
 * connect into Amazon EC2.
 */
public class Client {

    public static void main(String[] args) {
        ClientConfig clientConfig = new ClientConfig();
        ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
        AwsConfig awsConfig = new AwsConfig();

        awsConfig.setEnabled(true);
        awsConfig.setUsePublicIp(true);
        awsConfig.setProperty("access-key", "-- YOUR AMAZON ACCESS KEY --");
        awsConfig.setProperty("secret-key", "-- YOUR AMAZON SECRET KEY --");
        awsConfig.setProperty("region", "us-east-1");
        awsConfig.setProperty("security-group-name", "david-us-east-1-sg");
        awsConfig.setProperty("tag-key", "hazelcast_service");
        awsConfig.setProperty("tag-value", "true");

        clientConfig.setNetworkConfig(clientNetworkConfig.setAwsConfig(awsConfig));

        HazelcastInstance hazelcastClientInstance = HazelcastClient.newHazelcastClient(clientConfig);

        // Now do something...
        IMap<Object, Object> testMap = hazelcastClientInstance.getMap("test");
        testMap.put("testKey", "testValue");
    }
}
