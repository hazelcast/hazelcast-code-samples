package com.hazelcast.samples.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.PriorityBlockingQueue;

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
 */
public class MyPriorityQueueService implements ManagedService, RemoteService {

	private Map<String, PriorityBlockingQueue<?>> mapPriorityBlockingQueue
		= new HashMap<>();
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
	protected PriorityBlockingQueue<?> _getQueue(String name) {
		PriorityBlockingQueue<?> priorityBlockingQueue
			= this.mapPriorityBlockingQueue.get(name);
		
		if (priorityBlockingQueue==null) {
			priorityBlockingQueue = new PriorityBlockingQueue<>();
			this.mapPriorityBlockingQueue.put(name, priorityBlockingQueue);
		}
		
		return priorityBlockingQueue;
	}

}
