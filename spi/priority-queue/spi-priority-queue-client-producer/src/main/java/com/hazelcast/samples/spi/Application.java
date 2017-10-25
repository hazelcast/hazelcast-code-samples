package com.hazelcast.samples.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Write data into the queue, then shut down.
 * </p>
 */
@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		System.exit(0);
	}
	
	@Autowired
	private HazelcastInstance hazelcastInstance;

	/**
	 * <p>Write the orders in the order they were
	 * <u>created</u> into queues.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run(String... arg0) throws Exception {
		
		// Normal IQueue
		List<Order> orders = this.createOrders();
		
		IQueue<Order> vanilla = this.hazelcastInstance.getQueue("vanilla");
		for (Order order : orders) {
			vanilla.put(order);
		}
		log.info("Wrote {} into queue '{}', queue size now {}",
				orders.size(), vanilla.getName(), vanilla.size());

		// A distributed object of MyPriorityQueue type
		DistributedObject distributedObject
			= this.hazelcastInstance.getDistributedObject(MyPriorityQueue.SERVICE_NAME, "strawberry");
		MyPriorityQueue<Order> strawberry = (MyPriorityQueue<Order>) distributedObject;

		for (Order order : orders) {
			strawberry.offer(order);
		}
		log.info("Wrote {} into queue '{}', queue size now {}",
				orders.size(), strawberry.getName(), strawberry.size());

	}

	/**
	 * <p>The test data to insert in the queue,
	 * orders created due for Monday, Thursday, Friday, Thursday
	 * and Tuesday delivery.
	 * </p>
	 * 
	 * @return A list of orders, not in delivery sequence
	 */
	private List<Order> createOrders() {

		int seqNo=0;
		
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
