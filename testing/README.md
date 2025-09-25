# Testing samples

Testing applications that use Hazelcast (for caching, co-located compute and streaming) requires care to validate the behavior at
various levels - from
unit to system tests - given Hazelcast’s distributed, eventually/strongly consistent and asynchronous behavior.

Hazelcast provides tools to simplify writing unit/component/integration tests of such applications.

This project demonstrates the use of these tools. Full documentation to setup and configure your dependencies is available in
the [Hazelcast official documentation](https://docs.hazelcast.com/hazelcast/latest/test/testing-apps).

## Testing complex test scenarios

The sample code in this project illustrates how to test applications that use Hazelcast for caching and stream processing. It
includes two services: `Order` and `Customer` and it demonstrates testing each service independently as well as together.

Tests are available in both JUnit4 and JUnit5. The `com.hazelcast.samples.testing.samples` package contains basic testing support
API usage examples.

### Testing the integration of two services

In the following example, two services (`Customer` and `Order`) share state via Hazelcast. Functionality can be tested as
following:

```java
    @Test
    public void testCustomerAndOrderServicesIntegration() {
        // Create a shared Hazelcast instance
        instance = createHazelcastInstance();

        // Instantiate both services using same cluster
        CustomerService customerService = new HzCustomerService(instance);
        OrderService orderService = new HzOrderService(instance);

        // Add customer
        Customer alice = new Customer("c1", "Alice");
        customerService.save(alice);

        // Place an order for Alice
        Order order = new Order("o1", "c1", "Laptop");
        orderService.placeOrder(order);

        // Verify state across services
        assertEquals("Alice", customerService.findCustomer("c1").name());
        assertEquals("Laptop", orderService.getOrder("o1").product());
    }
```

### Testing a component integration with its dependencies

Another typical scenario consists of testing the integration of a component, in isolation, but integrated with its dependencies:

```java
    @Test
    public void customerServiceWithMapStoreInteractions()
            throws Exception {
        // Set up H2 in-memory DB
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        conn.createStatement().execute("CREATE TABLE customers (id VARCHAR PRIMARY KEY, name VARCHAR)");

        // Set up Hazelcast config with MapStore
        Config config = new Config();
        config.getMapConfig("customers").getMapStoreConfig().setEnabled(true).setImplementation(new CustomerMapStore(conn));

        hz = createHazelcastInstance(config);
        CustomerService service = new HzCustomerService(hz);

        // Act
        service.save(new Customer("c1", "Alice")); // should go into both IMap and DB
        Customer fromMap = service.findCustomer("c1");      // should be from IMap

        // Clear IMap to test reloading from DB
        hz.getMap("customers").evictAll();
        Customer fromStore = service.findCustomer("c1");    // should be reloaded from H2

        // Assert
        assertEquals("Alice", fromMap.name());
        assertEquals("Alice", fromStore.name());
    }

```

### Testing integrated behaviour

`HazelcastTestSupport` supports testing of the application using the Hazelcast capabilities, for example, in this case, the
execution of a listener:

```java
    @Test
    public void testOrderServiceListener() throws Exception {
        instance = createHazelcastInstance();
        // set a customer
        instance.getMap("customers").put("c1", new Customer("c1", "Alice"));

        OrderService sut = new HzOrderService(instance, mockConsumer);

        Order order = new Order("o1", "c1", "Laptop");
        sut.placeOrder(order);
        // Update the order so hazelcast triggers the event
        sut.updateOrder(order.confirm());
        
        // Verify that only mockConsumer#accept(Order) has been invoked, within 100ms
        verify(mockConsumer, timeout(100).only()).accept(any(Order.class));
    }
```

### Testing streaming applications

Test streaming applications is also supported - this is done extending `JetTestSupport` (itself an extension
of `HazelcastTestSupport`). The [Hazelcast docs](https://docs.hazelcast.com/hazelcast/latest/test/testing-streaming) provide
further details.

To use `JetTestSupport` the following dependencies must be included:

```xml
    <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-core</artifactId>
      <version>{log4j.version}</version> <!-- or whatever latest you want -->
    </dependency>
    <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-api</artifactId>
      <version>{log4j.version}</version>
    </dependency>
```

With `JetTestSupport` utility methods available, one can test distributed jobs like:

```java
    @Test
    public void testJetOrderEnrichmentWithHazelcastState() {
        HazelcastInstance instance = createHazelcastInstance();

        JetService jet = instance.getJet();

        IMap<String, Customer> customerMap = instance.getMap("customers");
        customerMap.put("c1", new Customer("c1", "Alice"));
        customerMap.put("c2", new Customer("c2", "Bob"));

        BatchSource<Order> source = TestSources.items(
                new Order("o1", "c1", "Laptop"),
                new Order("o2", "c2", "Phone")
        );
        Job job = jet.newJob(OrderEnrichmentPipeline.build(source));
        job.join(); // wait for completion

        IList<EnrichedOrder> result = instance.getList("enriched-orders");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(o -> o.customerName().equals("Alice")));
        assertTrue(result.stream().anyMatch(o -> o.customerName().equals("Bob")));
    }
```

The approach shown in the previous example doesn't work in cases where streams are never ending.
In this case, an approach based on a set of `assert*` utilities that can be injected in the pipeline is preferable.

For example, here we inject a data source and assert on the number of received items in the output stream.

```java
    @Test
    public void testStreamingEnrichmentWithInlineAssertion() {
        IMap<String, Customer> customerMap = instance.getMap("customers");
        customerMap.put("c1", new Customer("c1", "Alice"));
        customerMap.put("c2", new Customer("c2", "Bob"));

        // Streaming source
        StreamSource<Order> source = TestSources.itemStream(50, (ts, seq) -> {
            String customerId = seq % 2 == 0 ? "c1" : "c2";
            return new Order("o" + seq, customerId, "Product" + seq);
        });

        Pipeline pipeline = Pipeline.create();
        OrderEnrichmentPipeline.enrich(pipeline, source)
                               .apply(Assertions.assertCollectedEventually(5,
                                       list -> assertTrue("Expected at least 10 enriched orders", list.size() >= 10)));

        Job job = instance.getJet().newJob(pipeline);

        // The assertion will stop the job automatically via AssertionCompletedException by assertCollectedEventually
        try {
            job.join();
            fail("Expected job to terminate with AssertionCompletedException");
        } catch (CompletionException e) {
            if (!causedBy(e, AssertionCompletedException.class)) {
                throw e; // rethrow if it wasn't the expected assertion exit
            }
        }
    }
```

More assertions are available and [documented](https://docs.hazelcast.com/hazelcast/latest/test/testing#assertions). 