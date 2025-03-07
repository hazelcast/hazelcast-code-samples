package hazelcast.platform.labs.retail;

import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.pipeline.*;
import hazelcast.platform.labs.retail.domain.Customer;
import hazelcast.platform.labs.retail.domain.Inventory;
import hazelcast.platform.labs.retail.domain.Order;
import hazelcast.platform.labs.retail.domain.PickOrder;

import java.util.Map;

public class PickOrderPipeline {
    public static Pipeline createPipeline(){
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

        // Look up the item number in the inventory map.  Filter out orders for which there insufficient inventory
        // The first argument of the filterUsingService method is a ServiceFactory which retrieve the inventory map
        // The second argument is a lambda expression taking the inventory map and the order and returning true or false
        // This implementation silently discards orders that cannot be filled.  A more realistic solution would be
        // to have a second branch of the pipeline to handle un-fillable orders.
        StreamStage<Order> fillableOrders =
                orders.filterUsingService(ServiceFactories.<Integer, Inventory>iMapService("inventory"),
                (map, order) -> {
                    Inventory inventory = map.get(order.getItemNumber());
                    if (inventory == null) {
                        // warning, this item does not exist in inventory
                        return false;
                    }

                    return order.getQuantity() <= inventory.getQuantityInStock();
                });

        // In this stage we use mapUsingIMap to look up the customer associated with each order
        // The first argument is the name of the map
        // The second argument is a function which given an order, retrieves the key that will be used to join to the
        // customers map.
        // The last argument is a function which takes the retrieved customer and the order (from the event)
        // and produces a PickOrder

        StreamStage<PickOrder> pickOrders =
                fillableOrders.<Integer, Customer, PickOrder>mapUsingIMap("customers",
                    Order::getCustomerId,
                    (order, cust) -> {
                        PickOrder result = new PickOrder();
                        result.setOrderNumber(order.getOrderNumber());
                        result.setOrderTimestamp(order.getTimestamp());
                        result.setItemNumber(order.getItemNumber());
                        result.setQuantitiy(order.getQuantity());
                        result.setCustomerId(order.getCustomerId());
                        result.setLastName(cust == null ? "" : cust.getLastName());
                        result.setFirstName(cust == null ? "" : cust.getFirstName());
                        result.setAddress1(cust == null ? "" : cust.getAddress1());
                        result.setAddress2(cust == null ? "" : cust.getAddress2());
                        result.setPhone(cust == null ? "" : cust.getPhone());
                        return result;
                    });

        // finally, we transform the pick order into a Map.Entry<Integer, PickOrder> (Tuple2 implements Map.Entry)
        // and write the result to an IMap
        pickOrders.map( pickOrder -> Tuple2.tuple2( pickOrder.getOrderNumber(), pickOrder))
                .writeTo(Sinks.map("pick_orders"));

        return pipeline;
    }
}
