package com.hazelcast.samples.spi;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.PriorityQueue;

/**
 * A request to run {@link MyPriorityQueue#offer()},
 * and the {@link #run()} method that does the work on
 * the destination.
 */
@Getter
@Setter
@Slf4j
public class MyPriorityQueueOpOffer
        extends Operation
        implements PartitionAwareOperation {

    // Input
    private String name;
    private Object payload;
    // Output
    private Boolean response;

    /**
     * Run the {@code poll()} operation on the named
     * queue stored in the service.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void run() {
        log.trace("run() for '{}', payload '{}'", this.name, this.payload);

        MyPriorityQueueService myPriorityQueueService = super.getService();

        PriorityQueue queue
                = myPriorityQueueService.getQueue(this.name);

        this.response = queue.offer(this.payload);
    }

    /**
     * Serialize the operation fields to send from local to remote.
     */
    @Override
    protected void writeInternal(ObjectDataOutput objectDataOutput) throws IOException {
        super.writeInternal(objectDataOutput);
        objectDataOutput.writeUTF(this.name);
        objectDataOutput.writeObject(this.payload);
    }

    /**
     * De-serialize the operation fields to receive from remote to local.
     */
    @Override
    protected void readInternal(ObjectDataInput objectDataInput) throws IOException {
        super.readInternal(objectDataInput);
        this.name = objectDataInput.readUTF();
        this.payload = objectDataInput.readObject();
    }
}
