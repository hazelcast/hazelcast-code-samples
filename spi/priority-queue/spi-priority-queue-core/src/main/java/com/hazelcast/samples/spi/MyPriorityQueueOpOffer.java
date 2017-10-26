package com.hazelcast.samples.spi;

import java.io.IOException;
import java.util.PriorityQueue;

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
	private Object payload;
	// Output
	private Boolean response;
	
	/**
	 * <p>Run the {@code poll()} operation on the named
	 * queue stored in the service.
	 * </p>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void run() throws Exception {
		MyPriorityQueueService myPriorityQueueService = super.getService();

		PriorityQueue queue
			= myPriorityQueueService._getQueue(this.name);		
		
		this.response = queue.offer(this.payload);
	}

	
	/**
	 * <p>Serialize the operation fields to send from local to remote.
	 * </p>
	 */
    @Override
    protected void writeInternal(ObjectDataOutput objectDataOutput) throws IOException {
        super.writeInternal(objectDataOutput);
        objectDataOutput.writeUTF(this.name);
        objectDataOutput.writeObject(this.payload);
    }

	/**
	 * <p>De-serialize the operation fields to receive from remote to local.
	 * </p>
	 */
    @Override
    protected void readInternal(ObjectDataInput objectDataInput) throws IOException {
        super.readInternal(objectDataInput);
        this.name = objectDataInput.readUTF();
        this.payload = objectDataInput.readObject();
    }

}
