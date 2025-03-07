package com.hazelcast.hibernate.jcache.test.base;

import com.hazelcast.hibernate.jcache.entity.Item;
import com.hazelcast.hibernate.jcache.entity.SubItem;
import com.hazelcast.hibernate.jcache.test.util.SessionFactoryUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.stat.CacheRegionStatistics;
import org.hibernate.stat.Statistics;
import org.junit.After;

public class HibernateTestBase {

    public final SessionFactoryUtil sfUtil;

    public Statistics stats;

    public CacheRegionStatistics itemCacheStats;

    public CacheRegionStatistics subItemCacheStats;

    public CacheRegionStatistics collectionCacheStats;

    public HibernateTestBase(boolean useHazelcastClient){
        sfUtil = new SessionFactoryUtil(useHazelcastClient);
        insertEntries();
        stats = sfUtil.getStats();
        itemCacheStats = stats.getCacheRegionStatistics("Item-Cache");
        subItemCacheStats = stats.getCacheRegionStatistics("SubItem-Cache");
        collectionCacheStats = stats.getCacheRegionStatistics("SubItems-Collection-Cache");
    }

    @After
    public void reset(){
        // Evicts all cache regions and clear stats instead of
        // shutting down and creating a new SF & HZ for each test
        sfUtil.resetCache();
    }

    public void insertEntries(){

        Session session = sfUtil.getSessionFactory().openSession();

        Item item1 = new Item("item-1",1);
        Item item2 = new Item("item-2",2);
        Item item3 = new Item("item-3",3);
        Item item4 = new Item("item-4",4);

        SubItem subItem1 = new SubItem(1,"subitem-1",item1);
        SubItem subItem2 = new SubItem(2,"subitem-2",item2);
        SubItem subItem3 = new SubItem(3,"subitem-3",item2);
        SubItem subItem4 = new SubItem(4,"subitem-4",item3);
        SubItem subItem5 = new SubItem(5,"subitem-5",item3);
        SubItem subItem6 = new SubItem(6,"subitem-6",item3);
        SubItem subItem7 = new SubItem(7,"subitem-7",item4);
        SubItem subItem8 = new SubItem(8,"subitem-8",item4);
        SubItem subItem9 = new SubItem(9,"subitem-9",item4);
        SubItem subItem10 = new SubItem(10,"subitem-10",item4);

        item1.addSubItem(subItem1);
        item2.addSubItem(subItem2).addSubItem(subItem3);
        item3.addSubItem(subItem4).addSubItem(subItem5).addSubItem(subItem6);
        item4.addSubItem(subItem7).addSubItem(subItem8).addSubItem(subItem9).addSubItem(subItem10);

        session.save(item1);
        session.save(item2);
        session.save(item3);
        session.save(item4);

        Transaction tx = session.beginTransaction();
        tx.commit();
        session.close();

        sfUtil.resetCache();

    }

}
