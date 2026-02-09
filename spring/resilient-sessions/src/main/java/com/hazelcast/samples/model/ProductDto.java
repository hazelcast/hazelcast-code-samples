package com.hazelcast.samples.model;

import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;

public record ProductDto(long id, String name, BigDecimal listPrice) {

    public static class Serializer implements CompactSerializer<ProductDto> {

        @Override @NonNull
        public ProductDto read(CompactReader compactReader) {
            long id = compactReader.readInt64("id");
            String name = compactReader.readString("name");
            BigDecimal price = compactReader.readDecimal("listPrice");

            return new ProductDto(id, name, price);
        }

        @Override
        public void write(CompactWriter compactWriter, ProductDto productDto) {
            compactWriter.writeInt64("id", productDto.id());
            compactWriter.writeString("name", productDto.name());
            compactWriter.writeDecimal("listPrice", productDto.listPrice());
        }

        @Override @NonNull
        public String getTypeName() {
            return "ProductDto";
        }

        @Override @NonNull
        public Class<ProductDto> getCompactClass() {
            return ProductDto.class;
        }
    }
}
