package com.hazelcast.samples.eureka.partition.groups;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**FIXME TIDY TIDY TIDY!!
 * 
 */

//FIXME Build info doesn't appear in a browser ??!?!?
@SpringBootApplication
@EnableDiscoveryClient
public class MyHazelcastServer {

	static {
		
		//FIXME
		System.setProperty("spring.application.name", Constants.CLUSTER_NAME);
		
		/**
		 * <P>Pick ports for {@code bootstrap.yml}
		 * </P> 
		 */
		System.setProperty("my.web.port", String.valueOf(getNextPort(8080)));
		System.setProperty("my.hazelcast.port", String.valueOf(getNextPort(5701)));
		System.setProperty("my.hazelcast.host", findHostRemoteIp());
		
		// START : Meta Data extra
		System.setProperty("my.copenhagen.airport", "<terminal id=2/>");
		System.setProperty("my.flight.time", "{hours : \"17\", minutes \"20\"}");
		// -Dhazelcast.zone=zone1
		// System.setProperty("hazelcast.zone", "zone1");
		// END : Meta Data extra
	}

	/**
	 * XXX
	 * 
	 * @param args
	 */
    public static void main(String[] args) {
        SpringApplication.run(MyHazelcastServer.class, args);
    }

    /**
     * <P>
     * Return the next available port in sequence. Assumes it
     * stays available until we try to use it.
     * </P>
     * 
     * @param start - first port to try
     * @return An unused port greater or equal to the start port
     */
    private static int getNextPort(int start) {
    	int MAX = 100;
    	
		for (int port=start ; port < start+MAX ; port++) {
			try {
				new ServerSocket(port).close();
				return port;
			} catch (IOException portInUse) {
			}
		}
		
		return -1;
	}

    /**
     * <P>Find the remote IP of this host.
     * </P>
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
