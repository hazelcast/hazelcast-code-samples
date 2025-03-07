package com.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.core.HazelcastInstance;

import java.util.Properties;

public class example {
  public static void main(String[] args) throws Exception {
    ClientConfig config = new ClientConfig();
    config.getNetworkConfig().addAddress("<EXTERNAL-IP>");

    Properties clientSSLProps = new Properties();
    clientSSLProps.setProperty("trustStore", "example.jks");
    clientSSLProps.setProperty("trustStorePassword", "hazelcast");
    config.getNetworkConfig().setSSLConfig(new SSLConfig()
      .setEnabled(true)
      .setProperties(clientSSLProps));

    HazelcastInstance client = HazelcastClient.newHazelcastClient(config);
    System.out.println("Successful connection!");
  }
}
