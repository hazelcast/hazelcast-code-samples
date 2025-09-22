package com.hazelcast.samples.testing;

import com.hazelcast.function.BiFunctionEx;
import com.hazelcast.jet.pipeline.BatchSource;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;

public class OrderEnrichmentPipeline {
    public static Pipeline build(BatchSource<Order> source) {
        Pipeline p = Pipeline.create();
        p.readFrom(source).mapUsingIMap("customers", Order::customerId, getEnrichment()).writeTo(Sinks.list("enriched-orders"));
        return p;
    }

    private static BiFunctionEx<Order, Customer, EnrichedOrder> getEnrichment() {
        return (order, customer) -> {
            if (customer == null) {
                return null;
            }
            return new EnrichedOrder(order.id(), customer.name(), order.product());
        };
    }

    public static StreamStage<EnrichedOrder> enrich(Pipeline p, StreamSource<Order> source) {
        // Here you can use "with timestamps" if using event time
        return p.readFrom(source).withoutTimestamps()
                .mapUsingIMap("customers", Order::customerId, getEnrichment());
    }
}
