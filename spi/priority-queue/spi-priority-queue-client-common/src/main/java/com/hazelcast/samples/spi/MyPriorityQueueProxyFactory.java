package com.hazelcast.samples.spi;

import com.hazelcast.client.spi.ClientProxy;
import com.hazelcast.client.spi.ClientProxyFactory;

/**
 * <p>A factory to create proxies onto our distributed
 * object. 
 * </p>
 */
public class MyPriorityQueueProxyFactory<E> implements ClientProxyFactory {
	
	
	/**
	 * <p>Trigger the creation of the priority queue if needed.
	 * Whether created on demand or already existing on the
	 * servers, return a new proxy onto it from the client.
	 * </p>
	 * 
	 * @return A proxy onto the named priority queue
	 */
	@Override
	public ClientProxy create(String id) {
		return new MyPriorityQueueProxy<>(MyPriorityQueue.SERVICE_NAME, id, null);
	}

}
