package com.hazelcast.spring.jcache;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import static com.hazelcast.spring.jcache.DummyBeanTest.doInvocation;

public class SpringJCacheXMLConfig {

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");

        doInvocation(context);

        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }
}
