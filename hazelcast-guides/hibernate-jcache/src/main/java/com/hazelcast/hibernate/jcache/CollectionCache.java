package com.hazelcast.hibernate.jcache;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.hibernate.jcache.entity.Item;
import com.hazelcast.hibernate.jcache.entity.SubItem;
import com.hazelcast.hibernate.jcache.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class CollectionCache {

    public static void main(String[] args) {

        initializeDatabase();

        Session session;
        Item item;

        // Get Item from DB.
        // Collection cache miss & put are expected
        session = HibernateUtil.createNewSession();
        session.beginTransaction();
        item = session.get(Item.class, 1);

        // Since fetch type for the collection is lazy, force it to be fetched.
        for (SubItem i : item.getSubItems()) {
            System.out.println("Fetched:" + i);
        }

        session.close();
        System.out.println(HibernateUtil.getCollectionCacheStats());

        //Get the same Item again. Collection cache hit is expected.
        session = HibernateUtil.createNewSession();
        session.beginTransaction();
        item = session.get(Item.class, 1);

        // Since fetch type for the collection is lazy, force it to be fetched.
        for (SubItem i : item.getSubItems()) {
            System.out.println("Fetched:" + i);
        }

        session.close();
        System.out.println(HibernateUtil.getCollectionCacheStats());

        HibernateUtil.closeFactory();
        Hazelcast.shutdownAll();
    }

    private static void initializeDatabase() {
        Session session = HibernateUtil.createNewSession();
        Item item1 = new Item("item-1", 1);

        SubItem subItem1 = new SubItem(1, "subitem-1", item1);
        SubItem subItem2 = new SubItem(2, "subitem-2", item1);
        item1.addSubItem(subItem1).addSubItem(subItem2);

        Transaction tx = session.beginTransaction();
        session.save(item1);
        tx.commit();
        session.close();
        HibernateUtil.evictAllRegions();
    }
}
