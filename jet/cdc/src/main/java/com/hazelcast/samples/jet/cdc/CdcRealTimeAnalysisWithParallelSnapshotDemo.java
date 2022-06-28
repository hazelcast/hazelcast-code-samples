package com.hazelcast.samples.jet.cdc;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.cdc.ChangeRecord;
import com.hazelcast.jet.cdc.Operation;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.concurrent.TimeUnit;

import static com.hazelcast.jet.Traversers.singleton;
import static com.hazelcast.jet.Traversers.traverseItems;
import static com.hazelcast.jet.cdc.Operation.DELETE;
import static com.hazelcast.jet.cdc.Operation.INSERT;
import static com.hazelcast.jet.cdc.Operation.SYNC;
import static com.hazelcast.jet.cdc.postgres.PostgresCdcSources.postgres;
import static com.hazelcast.jet.datamodel.Tuple2.tuple2;
import static com.hazelcast.jet.pipeline.Sources.jdbc;
import static com.hazelcast.samples.jet.cdc.CdcRealTimeAnalysisWithParallelSnapshotDemo.ReportEvent.eventFor;
import static com.hazelcast.samples.jet.cdc.CdcRealTimeAnalysisWithParallelSnapshotDemo.ReportEvent.orderEvent;

/**
 * Same analytics scenario as in {@linkplain CdcRealTimeAnalysisDemo}, but with added reading from JDBC source
 * to speed up initial snapshot loading.
 */
public class CdcRealTimeAnalysisWithParallelSnapshotDemo {

    private static final long LAG = TimeUnit.DAYS.toMillis(1);

    @SuppressWarnings({ "resource", "checkstyle:MethodLength", "checkstyle:NeedBraces", "checkstyle:NPathComplexity" })
    public static void main(String[] args) {
        var config = new Config();
        config.getJetConfig().setEnabled(true);
        config.getSerializationConfig()
                .getCompactSerializationConfig()
                .register(CustomerStatsReport.class)
                .setEnabled(true);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        JetService jet = hz.getJet();
        DriverManager.getDrivers();
        // trigger initialization
        hz.getMap("CustomerStatsReport");

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

        Pipeline pipeline = Pipeline.create();
        var orderJdbc = pipeline.readFrom(
                jdbc(
                        () -> DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "cdcpwd1337"),
                        (connection, parallelism, index) -> {
                            String sql = "select * from inventory.orders where id % ? = ?";
                            PreparedStatement stmt = connection.prepareStatement(sql);
                            stmt.setInt(1, parallelism);
                            stmt.setInt(2, index);
                            return stmt.executeQuery();
                        },
                        rs -> {
                            Order order = new Order();
                            order.setId(rs.getInt("id"));
                            order.setOrderDate(rs.getDate("order_date"));
                            order.setPurchaser(rs.getInt("purchaser"));
                            order.setProductId(rs.getInt("product_id"));
                            order.setQuantity(rs.getInt("quantity"));
                            return orderEvent(order, Operation.SYNC);
                        })).addTimestamps(o -> o.event.getOrderDate().getTime(), LAG);

        var cdc = pipeline.readFrom(
                        postgres("inventory-db")
                                .setDatabaseName("postgres")
                                .setSchemaWhitelist("inventory")
                                .setDatabaseAddress("localhost")
                                .setDatabaseUser("postgres")
                                .setDatabasePassword("cdcpwd1337")
                                .setTableWhitelist("inventory.orders", "inventory.customers")
                                .setDatabasePort(5432)
                                .setCustomProperty("snapshot.mode", "initial")
                                .build())
                .withNativeTimestamps(LAG)
                .flatMap(record -> {
                    boolean isOrder = isOrder(record);
                    if (isOrder && record.operation() == SYNC) return Traversers.empty();
                    if (isOrder && record.operation() == Operation.UPDATE) {
                        return traverseItems(
                                orderEvent(record.newValue().toObject(Order.class), INSERT),
                                orderEvent(record.oldValue().toObject(Order.class), DELETE)
                        );
                    } else {
                        return eventFor(record, record.operation());
                    }
                });

        cdc.merge(orderJdbc)
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
                            // the order data may come from both JDBC and Debezium CDC source
                            // here we deduplicate the data, each order may be processed only one
                            // for production usage it's recommended to rethink this step and carefully pick
                            // which events will be taken.
                            if (!state.addProcessedOrderId(order.getId())) return state;
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
                .writeTo(Sinks.map("CustomerStatsReport"));
        var job = jet.newJob(pipeline);
        job.join();

    }

    private static boolean isOrder(ChangeRecord record) {
        return record.table().equalsIgnoreCase("Orders");
    }

    public record ReportEvent<E>(int customerId, Operation operation, E event) {
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
