package com.hazelcast.samples.testing;

import com.hazelcast.function.BiFunctionEx;
import com.hazelcast.jet.pipeline.BatchSource;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;

/**
 * Jet pipeline building blocks to enrich {@link Order} data with customer details.
 *
 * <p>Each order is joined with the {@code "customers"} map on {@code customerId}.
 * The result is an {@link EnrichedOrder} written to the target sink or stage.
 */
public class OrderEnrichmentPipeline {

    /**
     * Build a batch pipeline that enriches orders from the given source
     * and writes results to the {@code "enriched-orders"} list.
     *
     * @param source batch source of {@link Order} records
     * @return pipeline definition
     */
    public static Pipeline build(BatchSource<Order> source) {
        Pipeline p = Pipeline.create();
        p.readFrom(source)
         .mapUsingIMap("customers", Order::customerId, getEnrichment())
         .writeTo(Sinks.list("enriched-orders"));
        return p;
    }

    /**
     * Enrichment function joining an {@link Order} with its {@link Customer}.
     *
     * @return function producing {@link EnrichedOrder}, or {@code null} if no customer is found
     */
    private static BiFunctionEx<Order, Customer, EnrichedOrder> getEnrichment() {
        return (order, customer) -> {
            if (customer == null) {
                return null;
            }
            return new EnrichedOrder(order.id(), customer.name(), order.product());
        };
    }

    /**
     * Add a streaming enrichment stage to an existing pipeline.
     *
     * <p>Orders from the given source are joined with customers in the
     * {@code "customers"} map. Event time can be configured with
     * {@code withTimestamps} if required.
     *
     * @param p      pipeline to add the stage to
     * @param source stream source of {@link Order} records
     * @return stream stage of enriched orders
     */
    public static StreamStage<EnrichedOrder> enrich(Pipeline p, StreamSource<Order> source) {
        return p.readFrom(source)
                .withoutTimestamps()
                .mapUsingIMap("customers", Order::customerId, getEnrichment());
    }
}
