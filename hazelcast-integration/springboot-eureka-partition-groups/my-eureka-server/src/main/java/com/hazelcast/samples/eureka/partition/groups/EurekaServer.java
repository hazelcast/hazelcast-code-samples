package com.hazelcast.samples.eureka.partition.groups;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * <P>
 * Run with "{@code java -jar my-eureka-server-0.1-SNAPSHOT.jar}"
 * </P>
 * <P>
 * Eureka server will be available from
 * <a href="http://localhost:8761/">http://localhost:8761/</a>
 * </P>
 * <P>
 * Eureka should really be clustered.
 * </P>
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServer {

        public static void main(String[] args) {
                SpringApplication.run(EurekaServer.class, args);
        }
        
}
