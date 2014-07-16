package com.hazelcast.springHibernate;

import org.hibernate.FlushMode;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import java.util.List;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 16.07.2014.
 */

public class StockDaoImpl extends HibernateDaoSupport implements StockDao{

    public void save(Stock stock){
        getHibernateTemplate().save(stock);
    }

    public void update(Stock stock){
        getHibernateTemplate().update(stock);
    }

    public void delete(Stock stock){
        getHibernateTemplate().delete(stock);
    }

    public Stock findByStockCode(String stockCode){
        List list = getHibernateTemplate().find("from Stock where stockCode=?",stockCode);
        return (Stock)list.get(0);
    }

}
