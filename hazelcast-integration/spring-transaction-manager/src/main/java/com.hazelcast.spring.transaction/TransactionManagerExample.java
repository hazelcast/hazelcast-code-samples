package com.hazelcast.spring.transaction;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class TransactionManagerExample {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
        HazelcastInstance instance = (HazelcastInstance) context.getBean("instance");
        ServiceWithTransactionalMethod service = (ServiceWithTransactionalMethod) context.getBean("transactionalService");
        System.out.println("#######  BEGIN #######");
        System.out.println("#######  Call to transactional method #######");
        service.transactionalPut("key1", "value1");
        IMap<String, String> testMap = instance.getMap("testMap");
        System.out.println("Map contains \"key1\" : " + testMap.containsKey("key1"));
        System.out.println("####### Call to transactional method with exception  #######");
        try {
            service.transactionalPutWithException("key2", "value2");
        } catch (RuntimeException ex) {
        }
        System.out.println("Map contains \"key2\" : " + testMap.containsKey("key2"));
        System.out.println("#######  END #######");

        Hazelcast.shutdownAll();
    }
}
