package com.hazelcast.springHibernate;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 16.07.2014.
 */

public interface StockBo {

    void save(Stock stock);

    void update(Stock stock);

    void delete(Stock stock);

    Stock findByStockCode(String stockCode);

}
