package com.hazelcast.cdc.pojo;

import java.io.Serializable;
import java.util.Date;

public class ProductInv implements Serializable {

    public Long sku = 0L;

    public double stock = 0;
    public String name = null;
    public Date lastUpdated = null;

    public ProductInv() {}

    @Override
    public String toString() {
        return "ProductInv{" +
                "sku=" + sku +
                ", stock=" + stock +
                ", name='" + name + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
