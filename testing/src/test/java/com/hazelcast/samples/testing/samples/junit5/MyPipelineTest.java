package com.hazelcast.samples.testing.samples.junit5;

import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.collection.IList;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.test.GeneratorFunction;
import com.hazelcast.jet.pipeline.test.TestSources;
import com.hazelcast.map.IMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;

import static com.hazelcast.jet.pipeline.test.Assertions.assertAnyOrder;
import static com.hazelcast.jet.pipeline.test.Assertions.assertCollectedEventually;
import static com.hazelcast.jet.pipeline.test.Assertions.assertContains;
import static com.hazelcast.jet.pipeline.test.Assertions.assertOrdered;
import static com.hazelcast.test.HazelcastTestSupport.assertSizeEventually;
import static com.hazelcast.test.HazelcastTestSupport.spawn;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;


class MyPipelineTest {
    record Customer(String id, String name) implements Serializable {
    }

    static class MyCustomerGen implements Serializable, GeneratorFunction<Customer> {
        @Override
        public Customer generate(long timestamp, long nextId) {
            return new Customer("ID_" + timestamp, "NAME_" + nextId);
        }
    }

    @Test
    void testSimplePipeline() {
        TestHazelcastFactory factory = new TestHazelcastFactory();

        Config config = new Config();
        config.setJetConfig(new JetConfig().setEnabled(true));
        HazelcastInstance instance = factory.newHazelcastInstance(config);

        JetService jet = instance.getJet();

        Pipeline p = Pipeline.create();
        p.readFrom(TestSources.items(1, 2, 3)).writeTo(Sinks.list("out"));

        jet.newJob(p).join();

        IList<Integer> result = instance.getList("out");
        assertEquals(3, result.size());

        factory.shutdownAll();
    }

    @Test
    void assertPipelines() {
        TestHazelcastFactory factory = new TestHazelcastFactory();

        Config config = new Config();
        config.setJetConfig(new JetConfig().setEnabled(true));
        HazelcastInstance instance = factory.newHazelcastInstance(config);

        JetService jet = instance.getJet();

        Pipeline p = Pipeline.create();
        p.readFrom(TestSources.items(1, 2, 3))
         .apply(assertAnyOrder("unexpected", List.of(3, 2, 1)))
         .apply(assertOrdered("unexpected", List.of(1, 2, 3)))
         .apply(assertContains("unexpected", List.of(1, 2)))
         .writeTo(Sinks.logger());

        jet.newJob(p).join();

        factory.shutdownAll();

    }

    @Test
    void  assertPipelinesWithTimestamp() {
        TestHazelcastFactory factory = new TestHazelcastFactory();

        Config config = new Config();
        config.setJetConfig(new JetConfig().setEnabled(true));
        HazelcastInstance instance = factory.newHazelcastInstance(config);

        JetService jet = instance.getJet();

        int itemsPerSecond = 3;

        Pipeline p = Pipeline.create();
        p.readFrom(TestSources.itemStream(itemsPerSecond, new MyCustomerGen()))
         .withNativeTimestamps(0)
         .writeTo(Sinks.list("new_customers"));

        Job job = jet.newJob(p);
        spawn((Runnable) () -> {
            assertSizeEventually(15, instance.getList("new_customers"));
            job.cancel();
        });

        // this blocks until join throws cancellation job from the Runnable
        assertThrows(CancellationException.class, job::join);

        factory.shutdownAll();
    }

    @Test
    void  assertPipelinesWithoutTimestamp() {
        TestHazelcastFactory factory = new TestHazelcastFactory();

        Config config = new Config();
        config.setJetConfig(new JetConfig().setEnabled(true));
        HazelcastInstance instance = factory.newHazelcastInstance(config);

        JetService jet = instance.getJet();

        int itemsPerSecond = 3;

        Pipeline p = Pipeline.create();
        p.readFrom(TestSources.itemStream(itemsPerSecond, new MyCustomerGen()))
         .withoutTimestamps()
         .apply(assertCollectedEventually(5,
                 items -> Assertions.assertTrue(items.size() >= 15,
                 "did not receive at least 20 items")))
        .writeTo(Sinks.list("new_customers"));

        Job job = jet.newJob(p);
        assertThrows(CompletionException.class, job::join);

        factory.shutdownAll();
    }

    @Test
    void testEnrichmentPipeline() {
        TestHazelcastFactory factory = new TestHazelcastFactory();

        Config config = new Config();
        config.setJetConfig(new JetConfig().setEnabled(true));
        HazelcastInstance instance = factory.newHazelcastInstance(config);
        JetService jet = instance.getJet();

        // Set up customer map entries
        IMap<String, Customer> customers = instance.getMap("customers");
        customers.put("c1", new Customer("c1", "Alice"));
        customers.put("c2", new Customer("c2", "Bob"));

        // Build and run the pipeline
        Pipeline p = Pipeline.create();
        p.readFrom(TestSources.items("c1", "c2"))
         .mapUsingIMap("customers",
                 id -> id, (c, customer) -> ((Customer) customer).name())
         .writeTo(Sinks.list("enriched"));
        jet.newJob(p).join();

        // Validate the result
        IList<String> result = instance.getList("enriched");
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0));
        assertEquals("Bob", result.get(1));

        factory.shutdownAll();
    }

}
