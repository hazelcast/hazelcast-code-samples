package com.hazelcast.spring.springaware;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Example of enabling {@link com.hazelcast.spring.context.SpringAware}
 * with Java config.
 */
public class SpringAwareAnnotationJavaConfig {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        HazelcastInstance hazelcastInstance = context.getBean(HazelcastInstance.class);

        Future<String> f = hazelcastInstance.getExecutorService("test").submit(new SomeTask());
        System.out.println(f.get());

        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }
}
