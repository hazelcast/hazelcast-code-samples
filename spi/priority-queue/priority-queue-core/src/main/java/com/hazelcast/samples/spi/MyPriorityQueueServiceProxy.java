package com.hazelcast.samples.spi;

import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;

import java.util.concurrent.Future;

/**
 * This proxy object makes the {@link MyPriorityQueueService}
 * available on the server side. One server can invoke operations
 * via the proxy on queues that are hosted by other servers.
 * <p>
 * The {@link MyPriorityQueue#offer(Object)}, {@link MyPriorityQueue#poll()}
 * and {@link MyPriorityQueue#size()} methods are very similar
 * boilerplate code to send operations. This could be refactored,
 * but the duplication here is to make it easier to follow.
 */
public class MyPriorityQueueServiceProxy<E>
        extends AbstractDistributedObject<MyPriorityQueueService>
        implements MyPriorityQueue<E> {

    private String name;
    private NodeEngine nodeEngine;
    private int partitionId;

    protected MyPriorityQueueServiceProxy(String name, NodeEngine nodeEngine, MyPriorityQueueService myPriorityQueueService) {
        super(nodeEngine, myPriorityQueueService);
        this.name = name;
        this.nodeEngine = nodeEngine;
        this.partitionId = nodeEngine.getPartitionService().getPartitionId(this.name);
    }

    /**
     * {@link com.hazelcast.spi.AbstractDistributedObject AbstractDistributedObject}
     *
     * @return The service instance name
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * {@link com.hazelcast.spi.AbstractDistributedObject AbstractDistributedObject}
     *
     * @return The service name
     */
    @Override
    public String getServiceName() {
        return MyPriorityQueue.SERVICE_NAME;
    }

    /**
     * Requests the {@code offer()} operation.
     *
     * @param e Something to add to the queue, {@code @NotNull}
     * @return Always true as this queue is unbounded
     */
    @Override
    public boolean offer(E e) throws Exception {
        // Create the operation
        MyPriorityQueueOpOffer myPriorityQueueOpOffer = new MyPriorityQueueOpOffer();
        myPriorityQueueOpOffer.setName(this.name);
        myPriorityQueueOpOffer.setPayload(e);

        // Build the remote execution from service name, operation and object partition
        InvocationBuilder builder = this.nodeEngine.getOperationService()
                .createInvocationBuilder(MyPriorityQueue.SERVICE_NAME, myPriorityQueueOpOffer, this.partitionId);

        // Submit and wait for the result
        Future<Boolean> future = builder.invoke();
        return future.get();
    }

    /**
     * Requests the {@code poll()} operation.
     *
     * @return Null or an object, depending if the queue is empty
     */
    @Override
    public E poll() throws Exception {
        // Create the operation
        MyPriorityQueueOpPoll myPriorityQueueOpPoll = new MyPriorityQueueOpPoll();
        myPriorityQueueOpPoll.setName(this.name);

        // Build the remote execution from service name, operation and object partition
        InvocationBuilder builder = this.nodeEngine.getOperationService()
                .createInvocationBuilder(MyPriorityQueue.SERVICE_NAME, myPriorityQueueOpPoll, this.partitionId);

        // Submit and wait for the result
        Future<E> future = builder.invoke();
        return future.get();
    }

    /**
     * Requests the {@code size()} operation.
     *
     * @return The queue size
     */
    @Override
    public int size() throws Exception {
        // Create the operation
        MyPriorityQueueOpSize myPriorityQueueOpSize = new MyPriorityQueueOpSize();
        myPriorityQueueOpSize.setName(this.name);

        // Build the remote execution from service name, operation and object partition
        InvocationBuilder builder = this.nodeEngine.getOperationService()
                .createInvocationBuilder(MyPriorityQueue.SERVICE_NAME, myPriorityQueueOpSize, this.partitionId);

        // Submit and wait for the result
        Future<Integer> future = builder.invoke();
        return future.get();
    }
}
