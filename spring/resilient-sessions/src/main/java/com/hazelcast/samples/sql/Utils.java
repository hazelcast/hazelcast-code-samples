package com.hazelcast.samples.sql;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.samples.model.Basket;
import com.hazelcast.spring.session.AttributeValue;
import com.hazelcast.spring.session.BackingMapSession;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

public final class Utils {
    private Utils() {
    }
    public static Traverser<Entry<String, BasketItemDto>> expandSessionObject(HazelcastInstance hz,
                                                                              BackingMapSession session) {
        AttributeValue basketAttr = session.getAttribute("basket");
        if (basketAttr == null) {
            return Traversers.empty();
        }
        var basket = basketAttr.deserialize(hz, Basket.class);
        return Traversers.traverseIterable(basket.items())
                         .map(item -> {
                             var product = item.product();
                             var dto = new BasketItemDto(basket.principalName(),
                                     new Date(session.getLastAccessedTime().toEpochMilli()),
                                     product.id(), product.name(),
                                     product.listPrice(), item.orderPrice(), item.quantity());
                             return Map.entry(session.getId(), dto);
                         });
    }

    public static BackingMapSession getEntryValue(Entry<Long, BackingMapSession> entry) {
        return entry.getValue();
    }
}
