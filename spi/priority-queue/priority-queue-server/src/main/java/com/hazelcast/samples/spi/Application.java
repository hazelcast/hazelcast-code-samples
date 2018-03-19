package com.hazelcast.samples.spi;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.Bootstrap;

/**
 * A server in the cluster, with a command
 * line shell to interact with the queues
 * defined.
 */
@SpringBootApplication
public class Application {

    /**
     * Start Spring. Indirectly, a Hazelcast server instance is created in
     * this JVM by {@link com.hazelcast.samples.spi.ApplicationConfig ApplicationConfig}.
     *
     * @param args From command line
     */
    public static void main(String[] args) throws Exception {
        System.setProperty("hazelcast.logging.type", "slf4j");
        Bootstrap.main(args);
    }
}
