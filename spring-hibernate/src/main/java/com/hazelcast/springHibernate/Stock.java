package com.hazelcast.springHibernate;

import java.io.Serializable;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 16.07.2014.
 */

public class Stock implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long stockId;
    private String stockCode;
    private String stockName;

    public Stock() {
    }

    public Stock(String stockCode, String stockName) {
        this.stockCode = stockCode;
        this.stockName = stockName;
    }

    public Long getStockId() {
        return this.stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public String getStockCode() {
        return this.stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getStockName() {
        return this.stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    @Override
    public String toString() {
        return "Stock [stockCode=" + stockCode + ", stockId=" + stockId
                + ", stockName=" + stockName + "]";
    }
}
