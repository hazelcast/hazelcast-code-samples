package com.hazelcast.samples.eureka.partition.groups;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * <P>Start up a Hazelcast client.
 * </P>
 * <P>We need the {@code @EnableDiscoveryClient} to create
 * the Eureka <B>client</B> that tries to connect to the 
 * Eureka server instance(s) that are hopefully running.
 * </P.
 * <P>The key point here is an Eureuka client bean is
 * created, that connects to an external Eureka server or
 * server instances, based on the properties in the 
 * YML files.
 * </P>
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MyHazelcastClient {

	/**
	 * <P>Create a Hazelcast client that connects to the
	 * Hazelcast server(s). Since that's all we're trying
	 * to prove here, we then shut down. Obviously we would
	 * normally want a client to do a bit more than this.
	 * </P>
	 * 
	 * @param args If you want to provide them, not needed
	 */
    public static void main(String[] args) {
        SpringApplication.run(MyHazelcastClient.class, args);
        System.exit(0);
    }
}
