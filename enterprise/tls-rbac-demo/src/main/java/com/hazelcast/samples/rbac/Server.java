package com.hazelcast.samples.rbac;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.examples.helper.LicenseUtils;

public class Server {

    public static Config config() throws FileNotFoundException {
        Config config = new XmlConfigBuilder("resources/hazelcast.xml").build();
        config.setLicenseKey(LicenseUtils.ENTERPRISE_LICENSE_KEY);
        return config;
    }

    public void demo() throws FileNotFoundException {
        Hazelcast.newHazelcastInstance(config());
    }

    public static void main(String[] args) throws IOException {
        Server thisServer = new Server();
        thisServer.demo();
    }
}
