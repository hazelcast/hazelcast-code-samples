package com.hazelcast.hibernate.jcache.util;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.stat.CacheRegionStatistics;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    private static Session currentSession;

    static {
        try {
            Configuration cfg = new Configuration();
            cfg.configure("hibernate.cfg.xml");
            sessionFactory = cfg.buildSessionFactory();
        } catch (HibernateException e) {
            e.printStackTrace();
        }
    }

    public static Session createNewSession() {
        if (currentSession != null && currentSession.isOpen()) {
            currentSession.close();
        }
        currentSession = sessionFactory.openSession();
        return currentSession;
    }

    public static void evictAllRegions() {
        sessionFactory.getCache().evictAllRegions();
        sessionFactory.getStatistics().clear();
    }

    public static void closeFactory() {
        sessionFactory.close();
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static String getStats() {
        StringBuilder s = new StringBuilder();
        s.append(String.format("[L2C Hit]: %d\n", sessionFactory.getStatistics().getSecondLevelCacheHitCount()));
        s.append(String.format("[L2C Miss]: %d\n", sessionFactory.getStatistics().getSecondLevelCacheMissCount()));
        s.append(String.format("[L2C Put]: %d\n", sessionFactory.getStatistics().getSecondLevelCachePutCount()));
        return s.toString();
    }

    public static String getQueryCacheStats() {
        StringBuilder s = new StringBuilder();
        s.append(String.format("[Query Cache Hits]: %d\n", sessionFactory.getStatistics().getQueryCacheHitCount()));
        s.append(String.format("[Query Cache Miss]: %d\n", sessionFactory.getStatistics().getQueryCacheMissCount()));
        s.append(String.format("[Query Cache Put]: %d\n", sessionFactory.getStatistics().getQueryCachePutCount()));
        return s.toString();
    }

    public static String getCollectionCacheStats() {
        CacheRegionStatistics cs = HibernateUtil.getSessionFactory().getStatistics()
                .getCacheRegionStatistics("SubItems-Collection-Cache");
        StringBuilder s = new StringBuilder();
        s.append(String.format("[Collection Cache Hits]: %d\n", cs.getHitCount()));
        s.append(String.format("[Collection Cache Miss]: %d\n", cs.getMissCount()));
        s.append(String.format("[Collection Cache Put]: %d\n", cs.getPutCount()));
        return s.toString();
    }
}
