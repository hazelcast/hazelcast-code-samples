package com.hazelcast.samples.jet.cdc;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;


public record Order(
        int id,
        @JsonProperty("order_date")
        Date orderDate,
        int purchaser,
        int quantity,
        @JsonProperty("product_id")
        int productId
) {
}
