package com.hazelcast.samples.spi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Read data into the queue, then shut down.
 * Reading removes from the queue.
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
	 * <p>Read the orders from the queue. The
	 * {@link com.hazelcast.core.IQueue IQueue} has
	 * items in the sequence they were added.
	 * {@link MyPriorityQueue} applies a priority
	 * ordering, so items of higher priority are
	 * read first.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run(String... arg0) throws Exception {

		// Normal IQueue
		IQueue<Day> vanilla = this.hazelcastInstance.getQueue("vanilla");

		log.info("Queue '{}' has size {}",
				vanilla.getName(), vanilla.size());
		
		for (int i=0 ; !vanilla.isEmpty() ; i++) {
			log.info("Item {} => {}", i, vanilla.poll());
		}
		
		// MyPriorityQueue
		DistributedObject distributedObject
			= this.hazelcastInstance.getDistributedObject(MyPriorityQueue.SERVICE_NAME, "strawberry");
		MyPriorityQueue<Order> strawberry = (MyPriorityQueue<Order>) distributedObject;

		log.info("Queue '{}' has size {}",
				strawberry.getName(), strawberry.size());
		
		int max = strawberry.size();
		for (int i=0 ; i<max ; i++) {
			log.info("Item {} => {}", i, strawberry.poll());
		}
		
		
	}

}
