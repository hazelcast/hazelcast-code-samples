package com.hazelcast.samples.model;

import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;

public record BasketItem (ProductDto product, BigDecimal orderPrice, int quantity) {

    public static class Serializer implements CompactSerializer<BasketItem> {

        @Override
        @NonNull
        public BasketItem read(CompactReader compactReader) {
            ProductDto product = compactReader.readCompact("product");
            BigDecimal orderPrice = compactReader.readDecimal("orderPrice");
            int quantity = compactReader.readInt32("quantity");
            return new BasketItem(product, orderPrice, quantity);
        }

        @Override
        public void write(CompactWriter compactWriter, BasketItem basketItem) {
            compactWriter.writeCompact("product", basketItem.product);
            compactWriter.writeDecimal("orderPrice", basketItem.orderPrice);
            compactWriter.writeInt32("quantity", basketItem.quantity);
        }

        @Override @NonNull
        public String getTypeName() {
            return "BasketItem";
        }

        @Override @NonNull
        public Class<BasketItem> getCompactClass() {
            return BasketItem.class;
        }
    }
}
