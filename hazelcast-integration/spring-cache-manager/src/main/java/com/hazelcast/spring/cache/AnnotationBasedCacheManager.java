package com.hazelcast.spring.cache;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AnnotationBasedCacheManager {

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.getGroupConfig().setName("grp");
        config.getGroupConfig().setPassword("grp-pass");
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().addMember("127.0.0.1:5701");

        Hazelcast.newHazelcastInstance(config);

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        context.register(CachingConfiguration.class);
        context.register(DummyBean.class);
        context.refresh();

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
