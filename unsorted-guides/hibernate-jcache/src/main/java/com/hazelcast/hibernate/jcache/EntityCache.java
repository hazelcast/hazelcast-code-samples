package com.hazelcast.hibernate.jcache;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.hibernate.jcache.entity.Item;
import com.hazelcast.hibernate.jcache.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class EntityCache {

    public static void main(String[] args) {

        Session session;
        Transaction tx;

        // Store an Item with id = 1
        // L2C put is expected here.
        session = HibernateUtil.createNewSession();
        tx = session.getTransaction();
        tx.begin();
        session.save(new Item("item-1", 1));
        tx.commit();
        session.close();

        // Evict cache and clear statistics
        HibernateUtil.evictAllRegions();

        // Get recently stored value. Since the cache is evicted,
        // data is expected to be fetched from DB, not from the cache.
        // L2C miss & put are expected.
        session = HibernateUtil.createNewSession();
        session.beginTransaction();
        session.get(Item.class, 1);
        session.close();
        System.out.println(HibernateUtil.getStats());

        // Get last accessed data from another session.
        // Data is expected to be fetched from L2 Cache, not from database.
        // L2C hit is expected.
        session = HibernateUtil.createNewSession();
        session.beginTransaction();
        session.get(Item.class, 1);
        session.close();
        System.out.println(HibernateUtil.getStats());

        // Evict the cache and clear statistics
        HibernateUtil.evictAllRegions();

        // Data is expected to be fetched from database, not from L2C.
        // L2C miss & put are expected
        session = HibernateUtil.createNewSession();
        session.beginTransaction();
        session.get(Item.class, 1);
        session.close();
        System.out.println(HibernateUtil.getStats());

        HibernateUtil.closeFactory();
        Hazelcast.shutdownAll();
    }
}
