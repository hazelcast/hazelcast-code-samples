package com.hazelcast.samples.model;

import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;
import org.jspecify.annotations.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Main basket object representation
 * @param principalName username of the user that's logged into the shop and is putting items in the basket.
 * @param items a collection of items, added by the user
 */
public record Basket (String principalName, List<BasketItem> items) implements Serializable {

    public static class Serializer implements CompactSerializer<Basket> {

        @Override
        @NonNull
        public Basket read(CompactReader compactReader) {
            String principalName1 = compactReader.readString("principalName");
            BasketItem[] items1 = compactReader.readArrayOfCompact("items", BasketItem.class);
            assert items1 != null;
            return new Basket(principalName1, new ArrayList<>(List.of(items1)));
        }

        @Override
        public void write(CompactWriter compactWriter, Basket basket) {
            compactWriter.writeString("principalName", basket.principalName);
            compactWriter.writeArrayOfCompact("items", basket.items.toArray());
        }

        @Override @NonNull
        public String getTypeName() {
            return "Basket";
        }

        @Override @NonNull
        public Class<Basket> getCompactClass() {
            return Basket.class;
        }
    }
}
