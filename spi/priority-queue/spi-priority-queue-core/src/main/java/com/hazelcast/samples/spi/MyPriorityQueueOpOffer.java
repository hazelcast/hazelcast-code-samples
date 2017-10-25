package com.hazelcast.samples.spi;

import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>A request to run {@link MyPriorityQueue#offer()},
 * and the {@link #run()} method that does the work on
 * the destination.
 * </p>
 */
@Getter
@Setter
public class MyPriorityQueueOpOffer
	extends Operation
	implements PartitionAwareOperation {

	// Input
	private String name;
	private Order order;
	// Output
	private Boolean response;
	
	/**
	 * <p>Run the {@code poll()} operation on the named
	 * queue stored in the service.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run() throws Exception {
		MyPriorityQueueService myPriorityQueueService = super.getService();

		PriorityBlockingQueue<Order> queue
			= (PriorityBlockingQueue<Order>) 
			myPriorityQueueService._getQueue(this.name);		
		
		this.response = queue.offer(this.order);
	}

	
	/**
	 * <p>Serialize the operation fields to send from local to remote.
	 * </p>
	 */
    @Override
    protected void writeInternal(ObjectDataOutput objectDataOutput) throws IOException {
        super.writeInternal(objectDataOutput);
        objectDataOutput.writeUTF(this.name);
        objectDataOutput.writeObject(this.order);
        objectDataOutput.writeObject(this.response);
    }

	/**
	 * <p>De-serialize the operation fields to receive from remote to local.
	 * </p>
	 */
    @Override
    protected void readInternal(ObjectDataInput objectDataInput) throws IOException {
        super.readInternal(objectDataInput);
        this.name = objectDataInput.readUTF();
        this.order = objectDataInput.readObject();
        this.response = objectDataInput.readObject();
    }

}
