package com.hazelcast.spring.jcache;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class SpringJCache {

    public static void main(String[] args)
            throws InterruptedException {
        ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
        IDummyBean dummy = (IDummyBean)context.getBean("dummyBean");

        System.out.println("#######  BEGIN #######");
        System.out.println("####### first call to getName method #######");
        String city = dummy.getCity();
        System.out.println("city:" + city);
        System.out.println("####### second call to getName method  #######");
        city = dummy.getCity();
        System.out.println("city:"+ city);
        System.out.println("#######  END #######");
        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }
}
