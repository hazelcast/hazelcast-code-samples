package com.hazelcast.samples.amazon.ec2.client;

import com.hazelcast.aws.AWSClient;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientAwsConfig;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.List;

/**
 * Created by dbrimley on 23/12/14.
 */
public class Client {

    public static void main(String args[]){

        ClientConfig clientConfig = new ClientConfig();
        ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
        ClientAwsConfig awsConfig = new ClientAwsConfig();

        awsConfig.setInsideAws(false);
        awsConfig.setEnabled(true);
        awsConfig.setAccessKey("AKIAIUYNBDCKRZUCBDNQ");
        awsConfig.setSecretKey("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");
        awsConfig.setRegion("us-east-1");
        //awsConfig.setHostHeader("https://ec2.us-east-1.amazonaws.com");
        awsConfig.setSecurityGroupName("david-us-east-1-sg");
        awsConfig.setTagKey("hazelcast_service");
        awsConfig.setTagValue("true");

        clientConfig.setNetworkConfig(clientNetworkConfig.setAwsConfig(awsConfig));

        HazelcastInstance hazelcastClientInstance = HazelcastClient.newHazelcastClient(clientConfig);

        IMap<Object, Object> testMap = hazelcastClientInstance.getMap("test");

        testMap.put("testKey","testValue");

    }

}
