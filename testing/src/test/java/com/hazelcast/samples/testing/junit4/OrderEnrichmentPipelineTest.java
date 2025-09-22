package com.hazelcast.samples.testing.junit4;

import com.hazelcast.collection.IList;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.samples.testing.Customer;
import com.hazelcast.samples.testing.EnrichedOrder;
import com.hazelcast.samples.testing.Order;
import com.hazelcast.samples.testing.OrderEnrichmentPipeline;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.jet.core.JetTestSupport;
import com.hazelcast.jet.pipeline.BatchSource;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.test.AssertionCompletedException;
import com.hazelcast.jet.pipeline.test.Assertions;
import com.hazelcast.jet.pipeline.test.TestSources;
import com.hazelcast.map.IMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class OrderEnrichmentPipelineTest
        extends JetTestSupport {
    private HazelcastInstance instance;

    @Before
    public void setup() {
        Config config = new Config();
        config.setJetConfig(new JetConfig().setEnabled(true));

        instance = createHazelcastInstance(config);
    }

    @After
    public void teardown() {
        instance.shutdown();
    }

    @Test
    public void jetOrderEnrichmentWithHazelcastState() {

        JetService jet = instance.getJet();

        IMap<String, Customer> customerMap = instance.getMap("customers");
        customerMap.put("c1", new Customer("c1", "Alice"));
        customerMap.put("c2", new Customer("c2", "Bob"));

        BatchSource<Order> source = TestSources.items(new Order("o1", "c1", "Laptop"), new Order("o2", "c2", "Phone"));
        Job job = jet.newJob(OrderEnrichmentPipeline.build(source));
        job.join(); // wait for completion

        IList<EnrichedOrder> result = instance.getList("enriched-orders");
        assertEquals(2, result.size());
        assertThat(result)
                .extracting(EnrichedOrder::customerName)
                .containsExactlyInAnyOrder("Alice", "Bob");
    }

    @Test
    public void streamingEnrichmentWithInlineAssertion() {
        IMap<String, Customer> customerMap = instance.getMap("customers");
        customerMap.put("c1", new Customer("c1", "Alice"));
        customerMap.put("c2", new Customer("c2", "Bob"));

        // Streaming source
        StreamSource<Order> source = TestSources.itemStream(50, (ts, seq) -> {
            String customerId = seq % 2 == 0 ? "c1" : "c2";
            return new Order("o" + seq, customerId, "Product" + seq);
        });

        Pipeline pipeline = Pipeline.create();
        OrderEnrichmentPipeline.enrich(pipeline, source).apply(Assertions.assertCollectedEventually(5,
                list -> assertTrue("Expected at least 10 enriched orders", list.size() >= 10)));

        Job job = instance.getJet().newJob(pipeline);

        // The assertion will stop the job automatically via AssertionCompletedException by assertCollectedEventually
        assertThatThrownBy(job::join)
                .hasRootCauseInstanceOf(AssertionCompletedException.class);
    }
}
