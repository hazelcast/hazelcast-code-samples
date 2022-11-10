package com.hazelcast.samples.jet.cdc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.cdc.ChangeRecord;
import com.hazelcast.jet.cdc.Operation;
import com.hazelcast.jet.cdc.RecordPart;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;

import static com.hazelcast.jet.Traversers.singleton;
import static com.hazelcast.jet.Traversers.traverseItems;
import static com.hazelcast.jet.cdc.Operation.DELETE;
import static com.hazelcast.jet.cdc.Operation.INSERT;
import static com.hazelcast.jet.cdc.postgres.PostgresCdcSources.postgres;
import static com.hazelcast.jet.datamodel.Tuple2.tuple2;
import static com.hazelcast.samples.jet.cdc.CdcRealTimeAnalysisDemo.ReportEvent.eventFor;
import static com.hazelcast.samples.jet.cdc.CdcRealTimeAnalysisDemo.ReportEvent.orderEvent;

/**
 * Demonstrates a simple reporting pipeline, which will be attached to existing system without the need of any change
 * in the upstream system and will update some analytics based on actions done by customers.
 *
 * To run this example you have to have PostgreSQL database set up. You can quickly do this, by running in your terminal:
 * <pre>
 *     docker run -d --rm --name cdc-postgres -p 5432:5432 \
 *      -e POSTGRES_DB=postgres -e POSTGRES_USER=postgres \
 *      -e POSTGRES_PASSWORD=cdcpwd1337 debezium/example-postgres:1.9.3.Final
 * </pre>
 *
 * The image comes with some data already filled. Later you can add more data manually to test the queries.
 *
 * Results of the pipeline are visible in the {@code CustomerStatsReport} IMap or via {@code CustomerStatsReport} SQL
 * mapping.
 */
public class CdcRealTimeAnalysisDemo {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SuppressWarnings({ "resource", "checkstyle:MethodLength" })
    public static void main(String[] args) {
        var config = new Config();
        config.getJetConfig().setEnabled(true);
        config.getSerializationConfig()
                .getCompactSerializationConfig()
                .addClass(CustomerStatsReport.class);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        JetService jet = hz.getJet();
        // trigger initialization
        hz.getMap("CustomerStatsReport");

        hz.getSql().execute(
          """
                  CREATE MAPPING CustomerStatsReport (
                    customerId INT,
                    customerFirstName VARCHAR,
                    customerLastName VARCHAR,
                    ordersTotal INT,
                    itemsTotal INT,
                    itemsAvg DOUBLE)
                  TYPE IMap OPTIONS (
                    'keyFormat' = 'int',
                    'valueFormat' = 'compact',
                    'valueCompactTypeName'='CustomerStatsReport'
                  );
                  """
        );

        Pipeline pipeline = Pipeline.create().readFrom(
                        postgres("inventory-db")
                                .setDatabaseName("postgres")
                                .setSchemaWhitelist("inventory")
                                .setDatabaseAddress("localhost")
                                .setDatabaseUser("postgres")
                                .setDatabasePassword("cdcpwd1337")
                                .setDatabasePort(5432)
                                .setTableWhitelist("inventory.customers", "inventory.orders")
                                .build())
                .withoutTimestamps()
                .flatMap(record -> {
                    if (isOrder(record) && record.operation() == Operation.UPDATE) {
                        return traverseItems(
                                orderEvent(toObject(record.oldValue(), Order.class), DELETE),
                                orderEvent(toObject(record.newValue(), Order.class), INSERT)
                        );
                    } else {
                        return eventFor(record, record.operation());
                    }
                })
                .groupingKey(ReportEvent::customerId)
                .mapStateful(CustomerStatsReport::new, (state, key, record) -> {
                    if (record.event() instanceof Customer customer) {
                        state.updateCustomerData(customer);
                    } else  {
                        var order = (Order) record.event();
                        var operation = record.operation();
                        if (operation == Operation.SYNC || operation == INSERT) {
                            state.updateWithNew(order);
                        } else if (operation == DELETE) {
                           state.updateWithDeleted(order);
                        }
                    }
                    // we return state copy, so that we are sure it includes only changes from this
                    // map operation (not done after the map is done)
                    return CustomerStatsReport.copy(state);
                })
                .peek(s -> "State: \n" + s)
                .map(state -> tuple2(state.getCustomerId(), state))
                .writeTo(Sinks.map("CustomerStatsReport"))
                .getPipeline();
        var job = jet.newJob(pipeline);
        job.join();
    }

    private static boolean isOrder(ChangeRecord record) {
        return record.table().equalsIgnoreCase("Orders");
    }

    public record ReportEvent<E>(int customerId, Operation operation, E event) {
        static ReportEvent<Order> orderEvent(Order order, Operation operation) {
            return new ReportEvent<>(order.purchaser(), operation, order);
        }
        static ReportEvent<Customer> customerEvent(Customer customer, Operation operation) {
            return new ReportEvent<>(customer.id(), operation, customer);
        }
        static Traverser<ReportEvent<?>> eventFor(ChangeRecord record, Operation operation) {
            return singleton(
                    isOrder(record)
                            ? orderEvent(toObject(record.value(), Order.class), operation)
                            : customerEvent(toObject(record.value(), Customer.class), operation)
            );
        }
    }

    private static <T> T toObject(RecordPart part, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(part.toJson(), type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
