package com.hazelcast.hibernate.jcache.test.util;

import com.hazelcast.core.Hazelcast;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.stat.Statistics;

public class SessionFactoryUtil {

    private SessionFactory sessionFactory;

    private boolean useHazelcastClient;

    public SessionFactoryUtil(boolean useHazelcastClient) {
        this.useHazelcastClient = useHazelcastClient;
        sessionFactory = createSessionFactory();
    }

    private SessionFactory createSessionFactory() {

        Configuration configuration = new Configuration();
        String hibernateConfigURL;

        Hazelcast.shutdownAll();

        if (useHazelcastClient) {
             hibernateConfigURL = "jcache-client-hibernate.cfg.xml";
             Hazelcast.newHazelcastInstance();
        } else {
            hibernateConfigURL = "jcache-server-hibernate.cfg.xml";
        }

        configuration.configure(hibernateConfigURL);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        return sessionFactory;
    }

    public Statistics getStats(){
        return sessionFactory.getStatistics();
    }

    public void resetCache(){
        sessionFactory.getCache().evictAllRegions();
        sessionFactory.getStatistics().clear();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

}
