package com.hazelcast.samples.rbac;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.security.AccessControlException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

/**
 * The executable TimestampClient class starts a Hazelcast client and tries repeatedly in a loop to read and write from/to a
 * protected {@code "timestamps"} {@link IMap}.
 */
public class TimestampClient {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("com.hazelcast");

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
        IMap<String, String> map = null;
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
                    LocalDateTime localDateTime = LocalDateTime.now();
                    map.put("timestamp", localDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
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
            e.printStackTrace();
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

    static {
        java.util.logging.ConsoleHandler ch = new java.util.logging.ConsoleHandler();
        ch.setLevel(java.util.logging.Level.WARNING);
        LOGGER.addHandler(ch);
        LOGGER.setLevel(java.util.logging.Level.WARNING);
    }
}
