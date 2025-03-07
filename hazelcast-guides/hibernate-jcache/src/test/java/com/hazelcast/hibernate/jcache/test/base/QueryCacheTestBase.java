package com.hazelcast.hibernate.jcache.test.base;

import com.hazelcast.hibernate.jcache.entity.Item;
import com.hazelcast.hibernate.jcache.entity.SubItem;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.stat.CacheRegionStatistics;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

@Ignore
public class QueryCacheTestBase extends HibernateTestBase {

    private static final String ITEM_QUERY_STRING = "select i from Item i where i.id < 3";
    private static final String SUBITEM_QUERY_STRING = "select si from SubItem si where si.id > 5";
    private static final String SUBITEM_UPDATE_QUERY_STRING = "update SubItem si set si.name = 'updated' where si.id > 5";

    public QueryCacheTestBase(boolean useHazelcastClient) {
        super(useHazelcastClient);
    }

    @Test
    public void queryCacheBasicMissPutAndHitTest(){

        Session session = sfUtil.getSessionFactory().openSession();

        @SuppressWarnings("unchecked")
        List<Item> itemList =  session.createQuery(ITEM_QUERY_STRING)
                .setCacheable(true)
                .setCacheRegion("Item-Query-Cache").list();

        CacheRegionStatistics itemQueryCacheStats = stats.getQueryRegionStatistics("Item-Query-Cache");

        Assert.assertEquals(2,itemList.size());

        Assert.assertEquals(1,itemQueryCacheStats.getMissCount());
        Assert.assertEquals(1,itemQueryCacheStats.getPutCount());
        Assert.assertEquals(0,itemQueryCacheStats.getHitCount());

        session.close();

        session = sfUtil.getSessionFactory().openSession();

        session.createQuery(ITEM_QUERY_STRING)
                .setCacheable(true)
                .setCacheRegion("Item-Query-Cache")
                .getResultList();

        Assert.assertEquals(1,itemQueryCacheStats.getHitCount());

    }

    @Test
    public void queryCacheUpdateThenPutTest(){

        Session session = sfUtil.getSessionFactory().openSession();

        @SuppressWarnings("unchecked")
        List<SubItem> subitemList =  session.createQuery(SUBITEM_QUERY_STRING)
                .setCacheable(true)
                .setCacheRegion("SubItem-Query-Cache").list();

        CacheRegionStatistics subItemQueryCacheStats = stats.getQueryRegionStatistics("SubItem-Query-Cache");

        Assert.assertEquals(5,subitemList.size());

        Assert.assertEquals(1,subItemQueryCacheStats.getMissCount());
        Assert.assertEquals(1,subItemQueryCacheStats.getPutCount());
        Assert.assertEquals(0,subItemQueryCacheStats.getHitCount());

        Transaction tx = session.beginTransaction();
        session.createQuery(SUBITEM_UPDATE_QUERY_STRING).executeUpdate();
        tx.commit();
        session.close();


        session = sfUtil.getSessionFactory().openSession();

        SubItem si = session.get(SubItem.class,7);
        Assert.assertEquals("updated",si.getName());

        session.createQuery(SUBITEM_QUERY_STRING)
                .setCacheable(true)
                .setCacheRegion("SubItem-Query-Cache").list();

        Assert.assertEquals(2,subItemQueryCacheStats.getMissCount());
        Assert.assertEquals(2,subItemQueryCacheStats.getPutCount());
        Assert.assertEquals(0,subItemQueryCacheStats.getHitCount());

    }

}
