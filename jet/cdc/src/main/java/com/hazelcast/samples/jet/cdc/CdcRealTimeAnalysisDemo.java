package com.hazelcast.samples.jet.cdc;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.cdc.ChangeRecord;
import com.hazelcast.jet.cdc.Operation;
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
 * Results of the pipeline are visible in the {@code CustomerStatsReport} IMap or via {@code CustomerStatsReport} SQL
 * mapping.
 */
public class CdcRealTimeAnalysisDemo {

    public static void main(String[] args) {
        var config = new Config();
        config.getJetConfig().setEnabled(true);
        config.getSerializationConfig()
                .getCompactSerializationConfig()
                .register(CustomerStatsReport.class)
                .setEnabled(true);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        JetService jet = hz.getJet();
        hz.getMap("CustomerStatsReport"); // trigger initialization

        //noinspection resource
        hz.getSql().execute(
          """
                  create mapping CustomerStatsReport (
                    customerId int,
                    customerFirstName varchar,
                    customerLastName varchar,
                    ordersTotal int,
                    itemsTotal int,
                    itemsAvg double)
                  type IMap options (
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
                                orderEvent(record.newValue().toObject(Order.class), INSERT),
                                orderEvent(record.oldValue().toObject(Order.class), DELETE)
                        );
                    } else {
                        return eventFor(record, record.operation());
                    }
                })
                .groupingKey(ReportEvent::customerId)
                .mapStateful(CustomerStatsReport::new, (state, key, record) -> {

                    if (record.event() instanceof Customer customer) {
                        state.setCustomerFirstName(customer.firstName);
                        state.setCustomerLastName(customer.lastName);
                        state.setCustomerId(customer.id);

                    } else  {
                        var order = (Order) record.event();
                        var operation = record.operation();
                        if (operation == Operation.SYNC || operation == INSERT) {
                            state.setCustomerId(order.getPurchaser());
                            state.setItemsTotal(state.getItemsTotal() + order.getQuantity());
                            state.setOrdersTotal(state.getOrdersTotal() + 1);
                            state.setItemsAvg(state.getItemsTotal() * 1.0d / state.getOrdersTotal());
                        } else if (operation == DELETE) {
                            state.setItemsTotal(state.getItemsTotal() - order.getQuantity());
                            state.setOrdersTotal(state.getOrdersTotal() - 1);
                            state.setItemsAvg(state.getItemsTotal() * 1.0d / state.getOrdersTotal());
                        }
                    }
                    return state;
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

    public record ReportEvent<E> (int customerId, Operation operation, E event) {
        static ReportEvent<Order> orderEvent(Order order, Operation operation) {
            return new ReportEvent<>(order.getPurchaser(), operation, order);
        }
        static ReportEvent<Customer> customerEvent(Customer customer, Operation operation) {
            return new ReportEvent<>(customer.id, operation, customer);
        }
        static Traverser<ReportEvent<?>> eventFor(ChangeRecord record, Operation operation) {
            return singleton(
                    isOrder(record)
                            ? orderEvent(record.value().toObject(Order.class), operation)
                            : customerEvent(record.value().toObject(Customer.class), operation)
            );
        }
    }

}
