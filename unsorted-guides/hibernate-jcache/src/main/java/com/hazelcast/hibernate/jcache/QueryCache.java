package com.hazelcast.hibernate.jcache;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.hibernate.jcache.entity.Item;
import com.hazelcast.hibernate.jcache.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class QueryCache {

    private static final String QUERY_STRING = "select i from Item i where i.id < 102";
    private static final String QUERY_STRING2 = "select i from Item i where i.id > 101";
    private static final String QUERY_CACHE_REGION = "Item-Query-Cache";

    public static void main(String[] args) {

        Session session;
        Transaction tx;

        // Store Items with id = 100, 101 and 102
        session = HibernateUtil.createNewSession();
        tx = session.getTransaction();
        tx.begin();
        session.save(new Item("New Hazelcast Item-0", 100));
        session.save(new Item("New Hazelcast Item-1", 101));
        session.save(new Item("New Hazelcast Item-2", 102));
        tx.commit();
        session.close();

        // Evict cache and clear statistics
        HibernateUtil.evictAllRegions();

        // Get recently stored values via query.
        // Data is expected to be fetched from database, not from L2C since evicted.
        // QueryCache miss & put are expected
        session = HibernateUtil.createNewSession();
        executeQuery(QUERY_STRING, session);
        session.close();
        System.out.println(HibernateUtil.getQueryCacheStats());

        // Execute last executed query again from another session.
        // Data is expected to be fetched from query Cache, not from database.
        // QueryCache hit is expected
        session = HibernateUtil.createNewSession();
        executeQuery(QUERY_STRING, session);
        session.close();
        System.out.println(HibernateUtil.getQueryCacheStats());

        // Execute another query.
        // QueryCache miss & put are expected
        session = HibernateUtil.createNewSession();
        executeQuery(QUERY_STRING2, session);
        session.close();
        System.out.println(HibernateUtil.getQueryCacheStats());

        // Tear down
        HibernateUtil.closeFactory();
        Hazelcast.shutdownAll();
    }

    private static List<Item> executeQuery(String query, Session session) {
        Transaction tx;
        tx = session.getTransaction();
        tx.begin();
        return (List<Item>) session.createQuery(query)
                .setMaxResults(10).setCacheable(true).setCacheRegion(QUERY_CACHE_REGION).list();
    }
}
