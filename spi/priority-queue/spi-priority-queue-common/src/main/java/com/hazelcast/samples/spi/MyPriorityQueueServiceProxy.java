package com.hazelcast.samples.spi;

import java.util.concurrent.Future;

import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;

/**
 * <p>This proxy object makes the {@link MyPriorityQueueService}
 * available on the server side. One server can invoke operations
 * via the proxy on queues that are hosted by other servers.
 * </p>
 * <p>The {@link #offer()}, {@link #poll()} and {@link #size()}
 * methods are very similar boilerplate code to send operations.
 * This could be refactored, but the duplication here is to
 * make it easier to follow.
 * </p>
 */
public class MyPriorityQueueServiceProxy
	extends AbstractDistributedObject<MyPriorityQueueService> 
	implements MyPriorityQueue<Order> {

	private String name;
	
	protected MyPriorityQueueServiceProxy(String name, NodeEngine nodeEngine, MyPriorityQueueService myPriorityQueueService) {
		super(nodeEngine, myPriorityQueueService);
		this.name = name;
	}

	/**
	 * <p>{@link com.hazelcast.spi.AbstractDistributedObject AbstractDistributedObject}
	 * </p>
	 * 
	 * @return The service instance name
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * <p>{@link com.hazelcast.spi.AbstractDistributedObject AbstractDistributedObject}
	 * </p>
	 * 
	 * @return The service name
	 */
	@Override
	public String getServiceName() {
		return MyPriorityQueue.SERVICE_NAME;
	}

	/**
	 * <p>Requests the {@code offer()} operation.
	 * </p>
	 * 
	 * @param e Something to add to the queue, {@code @NotNull}
	 * @return Always true as this queue is unbounded
	 */
	@Override
	public boolean offer(Order order) throws Exception {
		// Create the operation
		MyPriorityQueueOpOffer myPriorityQueueOpOffer = new MyPriorityQueueOpOffer();
		myPriorityQueueOpOffer.setName(this.name);
		myPriorityQueueOpOffer.setOrder(order);
		
		// Find out how it's going to be submitted
		NodeEngine nodeEngine = this.getNodeEngine();

		// Find out which partition contains the queue based on the name
	    int partitionId = nodeEngine.getPartitionService().getPartitionId(this.name);
	    
	    // Build the remote execution from service name, operation and object partition
	    InvocationBuilder builder = nodeEngine.getOperationService()
	               .createInvocationBuilder(MyPriorityQueue.SERVICE_NAME, myPriorityQueueOpOffer, partitionId);

	    // Submit and wait for the result
	    Future<Boolean> future = builder.invoke();
	    return future.get();
	}
	

	/**
	 * <p>Requests the {@code poll()} operation.
	 * </p>
	 * 
	 * @return Null or an object, depending if the queue is empty
	 */
	@Override
	public Order poll() throws Exception {
		// Create the operation
		MyPriorityQueueOpPoll myPriorityQueueOpPoll = new MyPriorityQueueOpPoll();
		myPriorityQueueOpPoll.setName(this.name);
		
		// Find out how it's going to be submitted
		NodeEngine nodeEngine = this.getNodeEngine();

		// Find out which partition contains the queue based on the name
	    int partitionId = nodeEngine.getPartitionService().getPartitionId(this.name);
	    
	    // Build the remote execution from service name, operation and object partition
	    InvocationBuilder builder = nodeEngine.getOperationService()
	               .createInvocationBuilder(MyPriorityQueue.SERVICE_NAME, myPriorityQueueOpPoll, partitionId);

	    // Submit and wait for the result
	    Future<Order> future = builder.invoke();
	    return future.get();
	}

	
	/**
	 * <p>Requests the {@code size()} operation.
	 * </p>
	 * 
	 * @return The queue size
	 */
	@Override
	public int size() throws Exception {
		// Create the operation
		MyPriorityQueueOpSize myPriorityQueueOpSize = new MyPriorityQueueOpSize();
		myPriorityQueueOpSize.setName(this.name);
		
		// Find out how it's going to be submitted
		NodeEngine nodeEngine = this.getNodeEngine();

		// Find out which partition contains the queue based on the name
	    int partitionId = nodeEngine.getPartitionService().getPartitionId(this.name);
	    
	    // Build the remote execution from service name, operation and object partition
	    InvocationBuilder builder = nodeEngine.getOperationService()
	               .createInvocationBuilder(MyPriorityQueue.SERVICE_NAME, myPriorityQueueOpSize, partitionId);

	    // Submit and wait for the result
	    Future<Integer> future = builder.invoke();
	    return future.get();
	}
	
}
