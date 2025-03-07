package com.hazelcast.hibernate.jcache.test;

import com.hazelcast.hibernate.jcache.entity.Item;
import com.hazelcast.hibernate.jcache.entity.SubItem;
import com.hazelcast.hibernate.jcache.test.base.HibernateTestBase;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 *  Tests if the entities are created on DB as expected.
 */
public class DatabaseEntitiesTest extends HibernateTestBase {


    public DatabaseEntitiesTest() {
        super(false);
    }

    @Test
    public void testItemsOnDatabaseStartUp(){

        Session session = sfUtil.getSessionFactory().openSession();

        @SuppressWarnings("unchecked")
        List<Item> items = session.createQuery("SELECT i from Item i ").getResultList();

        Assert.assertEquals(4,items.size());

        for (Item i : items) {
            Assert.assertTrue(i.getName().equals("item-"+i.getId()));
        }

        session.close();

    }

    @Test
    public void testSubItemsOnDatabaseStartUp(){

        Session session = sfUtil.getSessionFactory().openSession();

        @SuppressWarnings("unchecked")
        List<SubItem> subItems = session.createQuery("SELECT si from SubItem si ").getResultList();

        Assert.assertEquals(10,subItems.size());

        for(SubItem si : subItems) {
            Assert.assertTrue(si.getName().equals("subitem-"+si.getId()));
        }

        session.close();

    }

    @Test
    public void testSubItemRelationsOfItems(){

        Session session = sfUtil.getSessionFactory().openSession();

        Item item1 = session.get(Item.class,1);
        Item item2 = session.get(Item.class,2);
        Item item3 = session.get(Item.class,3);
        Item item4 = session.get(Item.class,4);

        Assert.assertNotNull(item1);
        Assert.assertNotNull(item2);
        Assert.assertNotNull(item3);
        Assert.assertNotNull(item4);

        Assert.assertEquals(1,item1.getSubItems().size());
        Assert.assertEquals(2,item2.getSubItems().size());
        Assert.assertEquals(3,item3.getSubItems().size());
        Assert.assertEquals(4,item4.getSubItems().size());

        session.close();

    }
}
