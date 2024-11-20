package com.hazelcast.spring.springaware;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.context.SpringManagedContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Example of enabling {@link com.hazelcast.spring.context.SpringAware} with Java config.
 */
@Configuration
@ComponentScan
public class SpringAwareAnnotationJavaConfig {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ApplicationContext context = new AnnotationConfigApplicationContext(SpringAwareAnnotationJavaConfig.class);

        HazelcastInstance hazelcastInstance = context.getBean(HazelcastInstance.class);

        Future<String> f = hazelcastInstance.getExecutorService("test").submit(new SpringAwareAwareTask());
        System.out.println("Bean definition names are: " + f.get());

        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }

    @Bean
    public SpringManagedContext managedContext() {
        return new SpringManagedContext();
    }

    @Bean
    public HazelcastInstance instance(SpringManagedContext context) {
        Config config = new Config();
        config.setManagedContext(context);
        return Hazelcast.newHazelcastInstance(config);
    }
}
