package com.hazelcast.samples.sql;

import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public record BasketItemDto(String principalName, Date lastAccessedTime, long productId, String name,
                            BigDecimal listPrice, BigDecimal orderPrice, int quantity) {

    public static class Serializer implements CompactSerializer<BasketItemDto> {

        @Override
        @NonNull
        public BasketItemDto read(CompactReader reader) {
            LocalDateTime timestamp = reader.readTimestamp("lastAccessedTime");
            Date lastAccessedTime = timestamp == null ? null : Date.from(timestamp.toInstant(ZoneOffset.UTC));
            return new BasketItemDto(reader.readString("principalName"), lastAccessedTime,
                    reader.readInt64("productId"), reader.readString("name"),
                    reader.readDecimal("listPrice"), reader.readDecimal("orderPrice"), reader.readInt32("quantity"));
        }

        @Override
        public void write(CompactWriter writer, BasketItemDto item) {
            Date lastAccessedTime = item.lastAccessedTime();
            LocalDateTime timestamp = lastAccessedTime == null ? null
                    : LocalDateTime.ofInstant(Instant.ofEpochMilli(lastAccessedTime.getTime()), ZoneOffset.UTC);
            writer.writeString("principalName", item.principalName());
            writer.writeTimestamp("lastAccessedTime", timestamp);
            writer.writeInt64("productId", item.productId());
            writer.writeString("name", item.name());
            writer.writeDecimal("listPrice", item.listPrice());
            writer.writeDecimal("orderPrice", item.orderPrice());
            writer.writeInt32("quantity", item.quantity());
        }

        @Override
        @NonNull
        public String getTypeName() {
            return "BasketItemDto";
        }

        @Override
        @NonNull
        public Class<BasketItemDto> getCompactClass() {
            return BasketItemDto.class;
        }
    }
}
