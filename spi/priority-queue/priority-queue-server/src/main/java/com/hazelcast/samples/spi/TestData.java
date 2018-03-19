package com.hazelcast.samples.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Test data, just orders to add to the queues.
 */
public class TestData {

    /**
     * The test data to insert in the queue,
     * orders created due for Monday, Thursday, Friday, Thursday
     * and Tuesday delivery.
     *
     * @return A list of orders, not in delivery sequence
     */
    public static List<Order> createOrders() {
        int seqNo = 0;

        Order order1 = new Order();
        order1.setId(UUID.randomUUID());
        order1.setSeqNo(seqNo++);
        order1.setDueDate(Day.MONDAY);

        Order order2 = new Order();
        order2.setId(UUID.randomUUID());
        order2.setSeqNo(seqNo++);
        order2.setDueDate(Day.THURSDAY);

        Order order3 = new Order();
        order3.setId(UUID.randomUUID());
        order3.setSeqNo(seqNo++);
        order3.setDueDate(Day.FRIDAY);

        Order order4 = new Order();
        order4.setId(UUID.randomUUID());
        order4.setSeqNo(seqNo++);
        order4.setDueDate(Day.THURSDAY);

        Order order5 = new Order();
        order5.setId(UUID.randomUUID());
        order5.setSeqNo(seqNo++);
        order5.setDueDate(Day.TUESDAY);

        List<Order> result = new ArrayList<>();

        result.add(order1);
        result.add(order2);
        result.add(order3);
        result.add(order4);
        result.add(order5);

        return result;
    }
}
