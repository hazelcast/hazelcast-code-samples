package com.hazelcast.spring.cache;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class DeclarativeCacheManager {

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.getGroupConfig().setName("grp");
        config.getGroupConfig().setPassword("grp-pass");
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().addMember("127.0.0.1:5701");

        Hazelcast.newHazelcastInstance(config);
        ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
        IDummyBean dummy = (IDummyBean) context.getBean("dummyBean");

        System.out.println("#######  BEGIN #######");
        System.out.println("####### first call to getName method #######");
        String city = dummy.getCity();
        System.out.println("city: " + city);
        System.out.println("####### second call to getName method  #######");
        city = dummy.getCity();
        System.out.println("city: " + city);
        System.out.println("#######  END #######");
        Thread.sleep(2);
        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }
}
