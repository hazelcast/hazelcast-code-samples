package com.hazelcast.samples.spi;

import com.hazelcast.core.DistributedObject;

/**
 * <p>{@code MyPriorityQueue} is a distributed object
 * (distributed onto the IMDG grid), just like 
 * {@link com.hazelcast.core.IQueue IQueue},
 * {@link com.hazelcast.core.IMap IMap} and all
 * the other built-ins.
 * </p>
 * <p>Although we're going to create an implementation
 * of {@link java.util.concurrent.PriorityBlockingQueue PriorityBlockingQueue}
 * for the purposes of the example we keep it simple and
 * only specify one method to write to the queue and
 * one to read from it.
 * </p>
 */
public interface MyPriorityQueue<E> extends DistributedObject {

	public static final String SERVICE_NAME = "MyPriorityQueueService";
	
	// Write if not full
	public boolean offer(E e);

	// Read
	public E poll();

	public int size();
}
