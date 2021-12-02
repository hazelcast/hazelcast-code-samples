package com.hazelcast.samples.rbac;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.concurrent.TimeUnit;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class TimestampClient {

    private final File clientConfigFile;
    
    public TimestampClient(String fileName) {
        clientConfigFile = new File(requireNonNull(fileName));
        if (!clientConfigFile.isFile()) {
            System.err.println("Unable to find the client config file " + clientConfigFile);
            System.exit(2);
        }
    }

    public ClientConfig clientConfig() throws IOException {
        ClientConfig clientConfig = new XmlClientConfigBuilder(clientConfigFile).build();
        return clientConfig;
    }

    public HazelcastInstance createClientInstance() throws IOException {
        return HazelcastClient.newHazelcastClient(clientConfig());
    }

    public void demo() throws IOException {
        HazelcastInstance client = createClientInstance();
        IMap map = null;
        while (map == null) {
            try {
                map = client.getMap("timestamps");
            } catch (AccessControlException ae) {
                System.out.println("Unable to work with timestamps map: " + ae);
                sleep();
            }
        }

        try {
            while (true) {
                System.out.print("Reading timestamp: ");
                try {
                    System.out.println(map.get("timestamp"));
                } catch (AccessControlException ae) {
                    System.out.println(ae.getMessage());
                }
                System.out.print("Setting new timestamp: ");
                try {
                    map.put("timestamp", System.currentTimeMillis());
                    System.out.println("passed");
                } catch (AccessControlException ae) {
                    System.out.println(ae.getMessage());
                }
                sleep();
            }
        } finally {
            client.shutdown();
        }
    }

    protected void sleep() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
        }
    }

    public static void main(String[] args) throws IOException {
        String fileName = "resources/regular-hazelcast-client.xml";
        if (args.length > 1) {
            System.err.println("Unexpected number of arguments.");
            System.err.println();
            System.err.println("Usage:");
            System.err.println("\tjava TimestampClient [client-config.xml]");
            System.err.println();
            System.exit(1);
        } else if (args.length == 1) {
            fileName = args[0];
        }
        new TimestampClient(fileName).demo();
    }

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("com.hazelcast");
    static {
        java.util.logging.ConsoleHandler ch = new java.util.logging.ConsoleHandler();
        ch.setLevel(java.util.logging.Level.WARNING);
        logger.addHandler(ch);
        logger.setLevel(java.util.logging.Level.WARNING);
    }
}
