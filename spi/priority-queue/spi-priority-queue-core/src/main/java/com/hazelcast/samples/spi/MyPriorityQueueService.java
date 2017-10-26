package com.hazelcast.samples.spi;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;

/**
 * <p>Create a service for our priority queue, just like
 * the map service, the queue service and all the built-ins.
 * </p>
 * <p>This is a {@link com.hazelcast.spi.ManagedService ManagedService}
 * as Hazelcast manages it's lifecycle along with the Hazelcast
 * instance starting and stopping.
 * </p>
 * <p>This is a {@link com.hazelcast.spi.RemoteService RemoteService}
 * as other Hazelcast instances can access it. Hence we return a proxy
 * to the real thing for them to work with.
 * </p>
 * <p>This is <b>not</b> yet a
 * {@link com.hazelcast.spi.MigrationAwareService MigrationAwareService}.
 * It should be for more realistic use but would complicate the example
 * further.
 * </p>
 * <p><b>OPTIMISATION NOTE</b></p>The threading model of Hazelcast
 * is such that only one thread is responsible for a subset of partitions.
 * (Eg. {@code thread-1} looks after partitions 1 &amp; 2. {@code thread-2}
 * looks after partitions 3 &amp; 4. And so on.)
 * So no partition's operations are ever handled by two threads
 * concurrently. Hence we can use {@link java.util.PriorityQueue}
 * rather than {@link java.util.concurrent.PriorityBlockingQueue} and this
 * gives better performance.
 * </p>
 */
public class MyPriorityQueueService implements ManagedService, RemoteService {

	// See class comments, PriorityQueue not PriorityBlockingQueue
	private Map<String, PriorityQueue<?>> mapPriorityQueue
		= new ConcurrentHashMap<>();
	private NodeEngine nodeEngine;

	/**
	 * <p>{@link com.hazelcast.spi.ManagedService ManagedService}
	 * </p>
	 * <p>Save the {@link com.hazelcast.spi.NodeEngine NodeEngine}
	 * for later.
	 * </p>
	 * 
	 * @param nodeEngine The Hazelcast operation processor
	 * @param properties {@code @NotNull} but not used
	 */
	@Override
	public void init(NodeEngine nodeEngine, Properties properties) {
		this.nodeEngine = nodeEngine;
	}

	/**
	 * <p>{@link com.hazelcast.spi.ManagedService ManagedService}
	 * </p>
	 * <p>Not used in this example.
	 * </p>
	 */
	@Override
	public void reset() {
	}

	/**
	 * <p>{@link com.hazelcast.spi.ManagedService ManagedService}
	 * </p>
	 * <p>No action needed to shut down the service.
	 * </p>.
	 * 
	 * @param terminate Ignored
	 */
	@Override
	public void shutdown(boolean terminate) {
	}

	/**
	 * <p>{@link com.hazelcast.spi.RemoteService RemoteService}
	 * </p>
	 * <p>Create a remotely accessible service instance.
	 * </p>
	 * 
	 * @param objectName The name for the priority queue
	 * @return The priority queue with that name
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public DistributedObject createDistributedObject(String objectName) {
		return new MyPriorityQueueServiceProxy(objectName, this.nodeEngine, this);
	}

	/**
	 * <p>{@link com.hazelcast.spi.RemoteService RemoteService}
	 * </p>
	 * <p>No action needed to shut down the service instance.
	 * </p>.
	 * 
	 * @param objectName A named priority queue
	 */
	@Override
	public void destroyDistributedObject(String objectName) {
	}

	/**
	 * <p>Helper method to return a queue held in the service,
	 * creating if necessary.
	 * </p>
	 * 
	 * @param name The queue name
	 * @return     The queue
	 */
	protected PriorityQueue<?> _getQueue(String name) {
		PriorityQueue<?> priorityQueue
			= this.mapPriorityQueue.get(name);
		
		if (priorityQueue==null) {
			priorityQueue = new PriorityQueue<>();
			this.mapPriorityQueue.put(name, priorityQueue);
		}
		
		return priorityQueue;
	}

}
