package com.hazelcast.hibernate.jcache.test.base;

import com.hazelcast.hibernate.jcache.entity.Item;
import com.hazelcast.hibernate.jcache.entity.SubItem;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class EntityCacheTestBase extends HibernateTestBase {


    public EntityCacheTestBase(boolean useHazelcastClient) {
        super(useHazelcastClient);
    }

    @Test
    public void entityCacheBasicMissPutAndHitTest(){

        Session session = sfUtil.getSessionFactory().openSession();

        session.get(Item.class,1);

        Assert.assertEquals(1, itemCacheStats.getMissCount());
        Assert.assertEquals(1, itemCacheStats.getPutCount());
        Assert.assertEquals(0, itemCacheStats.getHitCount());


        session.get(Item.class,2);

        Assert.assertEquals(2, itemCacheStats.getMissCount());
        Assert.assertEquals(2, itemCacheStats.getPutCount());
        Assert.assertEquals(0, itemCacheStats.getHitCount());


        session.close(); // evict first level cache as well

        session = sfUtil.getSessionFactory().openSession();

        session.get(Item.class,1);

        Assert.assertEquals(2, itemCacheStats.getMissCount());
        Assert.assertEquals(2, itemCacheStats.getPutCount());
        Assert.assertEquals(1, itemCacheStats.getHitCount());

        session.close();

    }

    @Test
    public void entityCacheUpdatePutAndHitTest(){

        Session session = sfUtil.getSessionFactory().openSession();

        Item item = session.get(Item.class,1);

        item.setName("updated");

        Transaction tx = session.beginTransaction();
        tx.commit();

        session.close();

        session = sfUtil.getSessionFactory().openSession();

        item = session.get(Item.class,1);

        Assert.assertEquals("updated", item.getName());
        Assert.assertEquals(1, itemCacheStats.getMissCount());
        Assert.assertEquals(2, itemCacheStats.getPutCount());
        Assert.assertEquals(1, itemCacheStats.getHitCount());

        session.close();

    }

    @Test
    public void entityCachePutCollectionEntitiesIntoCacheTest(){

        Session session = sfUtil.getSessionFactory().openSession();

        Item item = session.get(Item.class,4);
        for(SubItem si : item.getSubItems()) System.out.println(si.getName());

        // This is not a collection cache test but only tests subitem cache on its own.
        Assert.assertEquals(4,subItemCacheStats.getPutCount());
        Assert.assertEquals(0,subItemCacheStats.getHitCount());
        Assert.assertEquals(0,subItemCacheStats.getMissCount());

        session.close();
        session = sfUtil.getSessionFactory().openSession();

        item = session.get(Item.class,4);
        for(SubItem si : item.getSubItems()) System.out.println(si.getName());

        Assert.assertEquals(4,subItemCacheStats.getPutCount());
        Assert.assertEquals(4,subItemCacheStats.getHitCount());
        Assert.assertEquals(0,subItemCacheStats.getMissCount());

    }

}
