package com.hazelcast.springHibernate;

import com.hazelcast.core.Hazelcast;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Application {

    public static void main(String[] args) {
        InitializeDB.start();

        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        DistributedMapDemonstrator distributedMapDemonstrator = context.getBean(DistributedMapDemonstrator.class);
        distributedMapDemonstrator.demonstrate();

        Hazelcast.shutdownAll();
    }
}
