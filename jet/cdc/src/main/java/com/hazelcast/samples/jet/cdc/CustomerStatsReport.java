package com.hazelcast.samples.jet.cdc;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CustomerStatsReport implements Serializable {

    private final Set<Integer> processedOrderIds = new HashSet<>();
    private int customerId;
    private String customerFirstName;
    private String customerLastName;

    private int ordersTotal;
    private int itemsTotal;
    private double itemsAvg;

    public static CustomerStatsReport copy(CustomerStatsReport other) {
        var newInstance = new CustomerStatsReport();
        newInstance.customerId = other.customerId;
        newInstance.customerFirstName = other.customerFirstName;
        newInstance.customerLastName = other.customerLastName;
        newInstance.ordersTotal = other.ordersTotal;
        newInstance.itemsTotal = other.itemsTotal;
        newInstance.itemsAvg = other.itemsAvg;
        newInstance.processedOrderIds.addAll(other.processedOrderIds);
        return newInstance;
    }

    public boolean addProcessedOrderId(Integer id) {
        return processedOrderIds.add(id);
    }

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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CustomerStatsReport that = (CustomerStatsReport) o;
        return customerId == that.customerId
                && ordersTotal == that.ordersTotal
                && itemsTotal == that.itemsTotal
                && Double.compare(that.itemsAvg, itemsAvg) == 0
                && Objects.equals(customerFirstName, that.customerFirstName)
                && Objects.equals(customerLastName, that.customerLastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, customerFirstName, customerLastName, ordersTotal, itemsTotal, itemsAvg);
    }

    @Override
    @SuppressWarnings("checkstyle:OperatorWrap")
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

    public void updateWithNew(Order order) {
        customerId = order.purchaser();
        ordersTotal++;
        itemsTotal = itemsTotal + order.quantity();
        itemsAvg = itemsTotal * 1.0d / ordersTotal;
    }

    public void updateWithDeleted(Order order) {
        itemsTotal = itemsTotal - order.quantity();
        ordersTotal = ordersTotal - 1;
        itemsAvg = itemsTotal * 1.0d / ordersTotal;
    }

    public void updateCustomerData(Customer customer) {
        customerFirstName = customer.firstName();
        customerLastName = customer.lastName();
        customerId = customer.id();
    }
}
