package com.hazelcast.springHibernate;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 16.07.2014.
 */

public class StockBoImpl implements StockBo{

    StockDao stockDao;

    public void setStockDao(StockDao stockDao) {
        this.stockDao = stockDao;
    }

    public void save(Stock stock){
        stockDao.save(stock);
    }

    public void update(Stock stock){
        stockDao.update(stock);
    }

    public void delete(Stock stock){
        stockDao.delete(stock);
    }

    public Stock findByStockCode(String stockCode){
        return stockDao.findByStockCode(stockCode);
    }
}
