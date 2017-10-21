package com.hazelcast.samples.spi;

import java.util.Properties;

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
 */
public class MyPriorityQueueService implements ManagedService, RemoteService {

	private NodeEngine nodeEngine;

	/**
	 * <p>{@link com.hazelcast.spi.ManagedService ManagedService}
	 * </p>
	 * <p>Save the {@link com.hazelcast.spi.NodeEngine NodeEngine}
	 * for later.
	 * </p>
	 * 
	 * @param nodeEngine The Hazelcast operation processor
	 * @param properties Empty here so ignored
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
	 * @param terminate ?
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
		return new MyPriorityQueueServiceProxy<>(objectName, this.nodeEngine, this);
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

}
