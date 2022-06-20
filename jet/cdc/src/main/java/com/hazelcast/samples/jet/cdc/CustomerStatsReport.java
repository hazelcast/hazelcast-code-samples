package com.hazelcast.samples.jet.cdc;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CustomerStatsReport implements Serializable {

    private int customerId;
    private String customerFirstName;
    private String customerLastName;

    private int ordersTotal;
    private int itemsTotal;
    private double itemsAvg;

    Set<Integer> processedOrderIds = new HashSet<>();

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public void setCustomerFirstName(String customerFirstName) {
        this.customerFirstName = customerFirstName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }

    public int getOrdersTotal() {
        return ordersTotal;
    }

    public void setOrdersTotal(int ordersTotal) {
        this.ordersTotal = ordersTotal;
    }

    public int getItemsTotal() {
        return itemsTotal;
    }

    public void setItemsTotal(int itemsTotal) {
        this.itemsTotal = itemsTotal;
    }

    public double getItemsAvg() {
        return itemsAvg;
    }

    public void setItemsAvg(double itemsAvg) {
        this.itemsAvg = itemsAvg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerStatsReport that = (CustomerStatsReport) o;
        return customerId == that.customerId && ordersTotal == that.ordersTotal && itemsTotal == that.itemsTotal && Double.compare(that.itemsAvg, itemsAvg) == 0 && Objects.equals(customerFirstName, that.customerFirstName) && Objects.equals(customerLastName, that.customerLastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, customerFirstName, customerLastName, ordersTotal, itemsTotal, itemsAvg);
    }

    @Override
    public String toString() {
        return "{" +
                "customerId=" + customerId +
                ", customerFirstName='" + customerFirstName + '\'' +
                ", customerLastName='" + customerLastName + '\'' +
                ", ordersTotal=" + ordersTotal +
                ", itemsTotal=" + itemsTotal +
                ", itemsAvg=" + itemsAvg +
                '}';
    }
}
