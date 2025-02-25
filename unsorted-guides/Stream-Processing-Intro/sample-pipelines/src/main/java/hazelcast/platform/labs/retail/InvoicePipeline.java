package hazelcast.platform.labs.retail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.datamodel.Tuple3;
import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.pipeline.*;
import hazelcast.platform.labs.retail.domain.Customer;
import hazelcast.platform.labs.retail.domain.Inventory;
import hazelcast.platform.labs.retail.domain.Invoice;
import hazelcast.platform.labs.retail.domain.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Properties;

public class InvoicePipeline {
    public static Pipeline createPipeline(Properties kafaConnectionProps, String kafkaTopic){
        Pipeline pipeline = Pipeline.create();

        // Create a streaming source which reads the orders from a map journal, which is a list of change events
        // associated with an IMap.  See https://docs.hazelcast.com/hazelcast/5.3/pipelines/stream-imap
        StreamSource<Map.Entry<Integer, Order>> orderSource =
                Sources.mapJournal("orders", JournalInitialPosition.START_FROM_OLDEST);

        // Here we read a stream of Map.Entry<Integer, Order> from the IMap event journal
        // We also specify how to obtain a timestamp from the Order.
        // See https://docs.hazelcast.com/hazelcast/5.3/pipelines/building-pipelines#adding-timestamps-to-a-streaming-job
        // for a discussion.  The map step selects the value part of the map entry so we end up with a stream of
        // Orders rather than a stream of map entries
        StreamStage<Order> orders =
                pipeline.readFrom(orderSource).withTimestamps(entry -> entry.getValue().getTimestamp(), 3000)
                        .map(Map.Entry::getValue);

        // Look up the customer for each order in an IMap.  This stage emits a (order, customer) tuple
        // Note that the customer part of the tuple will be null if there is an order with no matching
        // customer_id in the customers map
        StreamStage<Tuple2<Order, Customer>> ordersAndCustomers =
                orders.<Integer, Customer, Tuple2<Order, Customer>>mapUsingIMap("customers",
                        Order::getCustomerId,
                        (order, customer) -> Tuple2.tuple2(order, customer));

        // This stage looks up the inventory item in the inventory IMap and adds the matching
        // inventory item to the tuple resulting in an (order, customer, inventory item) tuple.
        // Note that the inventory par of the tuple will be null if there is an order with no matching
        // item number in the inventory map
        StreamStage<Tuple3<Order, Customer, Inventory>> ordersAndCustomersAndInventory =
                ordersAndCustomers.<Integer, Inventory, Tuple3<Order, Customer, Inventory>>mapUsingIMap(
                        "inventory",
                        t2 -> t2.f0().getItemNumber(),
                        (t2, inventory) -> Tuple3.tuple3(t2.f0(), t2.f1(), inventory));

        // This is a simple mapping stage.  Here we see that functions can be defined elsewhere.  They do
        // not need to be defined inline.  This can make the pipeline code easier to read.
        StreamStage<Invoice> invoices =
                ordersAndCustomersAndInventory.map(t3 -> makeInvoice(t3.f0(), t3.f1(), t3.f2()));

        // In preparation for sending to external systems we convert the invoice to a JSON string
        // Note that we use mapUsingService so that we do create a new ObjectMapper to process
        // each event. See: https://docs.hazelcast.com/hazelcast/5.3/pipelines/transforms#mapusingservice
        StreamStage<String> jsonInvoices = invoices.mapUsingService(
                ServiceFactories.sharedService(ctx -> new ObjectMapper()),
                (mapper, invoice) -> mapper.writeValueAsString(invoice));

        // Create a Kafka sink using the passed connection properties and topic and write all invoices to it.
        // In this case we are not setting a key and the value will be a JSON encoded Invoice
        Sink<String> kafkaSink = KafkaSinks.<String>kafka(kafaConnectionProps).topic(kafkaTopic).build();
        jsonInvoices.writeTo(kafkaSink);

        return pipeline;
    }

    private static Invoice makeInvoice(Order order, Customer customer, Inventory item){
        Invoice result = new Invoice();

        result.setOrderNumber(order.getOrderNumber());
        result.setCustomerId( customer == null ? 0 : customer.getCustomerId());
        result.setLastName( customer == null ? "": customer.getLastName());
        result.setFirstName( customer == null ? "": customer.getLastName());
        result.setPhone( customer == null ? "": customer.getPhone());


        BigDecimal total = BigDecimal.ZERO;
        if (item != null){
            total = item.getUnitPrice().multiply(BigDecimal.valueOf(order.getQuantity()))
                    .setScale(2, RoundingMode.HALF_EVEN);
        }
        result.setOrderTotal(total);

        return result;
    }
}
