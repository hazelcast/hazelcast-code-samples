package com.hazelcast.samples.amazon.ec2.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientAwsConfig;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

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
        ClientAwsConfig awsConfig = new ClientAwsConfig();

        awsConfig.setInsideAws(false);
        awsConfig.setEnabled(true);
        awsConfig.setAccessKey("-- YOUR AMAZON ACCESS KEY --");
        awsConfig.setSecretKey("-- YOUR AMAZON SECRET KEY --");
        awsConfig.setRegion("us-east-1");
        awsConfig.setSecurityGroupName("david-us-east-1-sg");
        awsConfig.setTagKey("hazelcast_service");
        awsConfig.setTagValue("true");

        clientConfig.setNetworkConfig(clientNetworkConfig.setAwsConfig(awsConfig));

        HazelcastInstance hazelcastClientInstance = HazelcastClient.newHazelcastClient(clientConfig);

        // Now do something...
        IMap<Object, Object> testMap = hazelcastClientInstance.getMap("test");
        testMap.put("testKey", "testValue");
    }
}
