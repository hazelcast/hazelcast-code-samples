package com.hazelcast.samples.eureka.partition.groups;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Run with "{@code java -jar my-eureka-client-0.1-SNAPSHOT.jar}"
 * <p>
 * Connect to the Eureka server to list what is registered. This is also
 * available as
 * <a href="http://localhost:8761/eureka/apps">http://localhost:8761/eureka/apps</a>
 * but not as easy to
 * read as it's XML.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MyEurekaClient implements CommandLineRunner {

    @Autowired
    DiscoveryClient discoveryClient;

    public static void main(String[] args) {
        SpringApplication.run(MyEurekaClient.class, args);
        System.exit(0);
    }

    @Override
    public void run(String... arg0) throws Exception {
        System.out.println("");
        System.out.format("--------------------------------------------------------------------------------%n");

        System.out.format("\tSee http://localhost:8761/eureka/apps/%s%n",
                Constants.CLUSTER_NAME);

        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(Constants.CLUSTER_NAME);

        for (int i = 0; i < serviceInstances.size(); i++) {
            ServiceInstance serviceInstance = serviceInstances.get(i);
            System.out.format("\t\t(%d) %s%n", i, serviceInstance.getUri().toURL());

            Map<String, String> metaData = serviceInstance.getMetadata();
            if (metaData.size() == 0) {
                System.out.format("\t\t\t    *** NO METADATA ***%n");
            } else {
                System.out.format("\t\t\tMetadata%n");

                // Alphabetical ordering
                Set<String> keys = new TreeSet<>(metaData.keySet());

                for (String key : keys) {
                    String value = metaData.get(key);
                    System.out.format("\t\t\t -> %-20s %s%n", key, value);
                }
            }
        }

        System.out.format("\t[%d instance%s]%n", serviceInstances.size(), (serviceInstances.size() == 1 ? "" : "s"));

        System.out.format("--------------------------------------------------------------------------------%n");
        System.out.println("");
    }
}
