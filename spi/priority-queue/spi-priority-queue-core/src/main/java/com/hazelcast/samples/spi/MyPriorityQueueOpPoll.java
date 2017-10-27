package com.hazelcast.samples.spi;

import java.io.IOException;
import java.util.PriorityQueue;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>A request to run {@link MyPriorityQueue#poll()},
 * and the {@link #run()} method that does the work on
 * the destination.
 * </p>
 */
@Getter
@Setter
@Slf4j
public class MyPriorityQueueOpPoll
    extends Operation
    implements PartitionAwareOperation {

    // Input
    private String name;
    // Output
    private Object response;

    /**
     * <p>Run the {@code poll()} operation on the named
     * queue stored in the service.
     * </p>
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void run() throws Exception {
        log.trace("run() for '{}'", this.name);

        MyPriorityQueueService myPriorityQueueService = super.getService();

        PriorityQueue queue
            = myPriorityQueueService.getQueue(this.name);

        this.response = queue.poll();
    }


    /**
     * <p>Serialize the operation fields to send from local to remote.
     * </p>
     */
    @Override
    protected void writeInternal(ObjectDataOutput objectDataOutput) throws IOException {
        super.writeInternal(objectDataOutput);
        objectDataOutput.writeUTF(this.name);
    }

    /**
     * <p>De-serialize the operation fields to receive from remote to local.
     * </p>
     */
    @Override
    protected void readInternal(ObjectDataInput objectDataInput) throws IOException {
        super.readInternal(objectDataInput);
        this.name = objectDataInput.readUTF();
    }

}
