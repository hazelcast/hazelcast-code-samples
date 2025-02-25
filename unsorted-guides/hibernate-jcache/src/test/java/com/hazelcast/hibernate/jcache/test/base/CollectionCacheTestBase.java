package com.hazelcast.hibernate.jcache.test.base;

import com.hazelcast.hibernate.jcache.entity.Item;
import com.hazelcast.hibernate.jcache.entity.SubItem;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CollectionCacheTestBase extends HibernateTestBase {

    public CollectionCacheTestBase(boolean useHazelcastClient) {
        super(useHazelcastClient);
    }

    @Test
    public void collectionCacheHitWhenOwnerIsFetchedTest(){

        Item item;

        Session session = sfUtil.getSessionFactory().openSession();

        item = session.get(Item.class,1);
        for(SubItem si : item.getSubItems()) System.out.println(si.getName());

        item = session.get(Item.class,3);
        for(SubItem si : item.getSubItems()) System.out.println(si.getName());

        Assert.assertEquals(2,collectionCacheStats.getMissCount());
        Assert.assertEquals(2,collectionCacheStats.getPutCount());
        Assert.assertEquals(0,collectionCacheStats.getHitCount());

        session.close(); // evict first level cache

        session = sfUtil.getSessionFactory().openSession();

        item = session.get(Item.class,3);
        for(SubItem si : item.getSubItems()) System.out.println(si.getName());

        Assert.assertEquals(1,collectionCacheStats.getHitCount());

        session.close();

    }

    @Test
    public void collectionCacheUpdateAndPutTest(){

        Session session = sfUtil.getSessionFactory().openSession();

        Item item = session.get(Item.class, 1);
        item.getSubItems().get(0).setName("updated subitem");

        session.update(item);
        Transaction tx = session.beginTransaction();
        tx.commit();
        session.close();

        Assert.assertEquals(0,collectionCacheStats.getHitCount());
        Assert.assertEquals(1,collectionCacheStats.getMissCount());
        Assert.assertEquals(1,collectionCacheStats.getPutCount());

        Assert.assertEquals(2,subItemCacheStats.getPutCount());

        session = sfUtil.getSessionFactory().openSession();
        item = session.get(Item.class,1);

        Assert.assertEquals("updated subitem", item.getSubItems().get(0).getName());

        session.close();

    }

}
