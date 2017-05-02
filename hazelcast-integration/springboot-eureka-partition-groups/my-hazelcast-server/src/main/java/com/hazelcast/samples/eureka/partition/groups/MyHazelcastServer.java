package com.hazelcast.samples.eureka.partition.groups;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Collections;

import static com.hazelcast.util.EmptyStatement.ignore;

/**
 * Create a Hazelcast instance that can be one of many running on the
 * same host.
 * <p>
 * To run multiple on the same host means avoiding a port clash,
 * and normally we would let Hazelcast pick any unassigned port for
 * this. However, to give greater control, we find available ports
 * before the {@code main()} method starts, so that we can register
 * them with Eureka.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MyHazelcastServer {

    private static final int MAX_PORT = 100;

    static {
        System.setProperty("spring.application.name", Constants.CLUSTER_NAME);

        // pick ports for {@code bootstrap.yml}
        String s = String.valueOf(getNextPort(8081));
        System.setProperty("my.web.port", s);
        System.setProperty("server.port", s);
        System.setProperty("my.hazelcast.port", String.valueOf(getNextPort(5701)));
        System.setProperty("my.hazelcast.host", findHostRemoteIp());
    }

    /**
     * Start this process as a Spring application, which will start
     * up a {@code HazelcastInstance} using the above properties.
     */
    public static void main(String[] args) {
        SpringApplication.run(MyHazelcastServer.class, args);
    }

    /**
     * Return the next available port in sequence. Assumes it
     * stays available until we try to use it.
     *
     * @param start - first port to try
     * @return An unused port greater or equal to the start port
     */
    private static int getNextPort(int start) {
        for (int port = start; port < start + MAX_PORT; port++) {
            try {
                new ServerSocket(port).close();
                return port;
            } catch (IOException portInUse) {
                ignore(portInUse);
            }
        }
        return -1;
    }

    /**
     * Find the remote IP of this host.
     *
     * @return The non-localhost IP address as a String.
     */
    private static String findHostRemoteIp() {
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
                    if (inetAddress instanceof Inet4Address) {
                        if (!"127.0.0.1".equals(inetAddress.getHostAddress())) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }
}
