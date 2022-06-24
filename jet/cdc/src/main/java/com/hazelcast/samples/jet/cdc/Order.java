package com.hazelcast.samples.jet.cdc;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@SuppressWarnings("checkstyle:VisibilityModifier")
public class Order implements Serializable {

    public int id;
    @JsonProperty("order_date")
    public Date orderDate;
    public int purchaser;
    public int quantity;
    @JsonProperty("product_id")
    public int productId;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Date getOrderDate() {
        return orderDate;
    }
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
    public int getPurchaser() {
        return purchaser;
    }
    public void setPurchaser(int purchaser) {
        this.purchaser = purchaser;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public int getProductId() {
        return productId;
    }
    public void setProductId(int productId) {
        this.productId = productId;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Order order = (Order) o;
        return id == order.id
                && purchaser == order.purchaser
                && quantity == order.quantity
                && productId == order.productId
                && Objects.equals(orderDate, order.orderDate);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id, orderDate, purchaser, quantity, productId);
    }


}
