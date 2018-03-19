package com.hazelcast.samples.spi;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * A request to run {@link MyPriorityQueue#size()},
 * and the {@link #run()} method that does the work on
 * the destination.
 */
@Getter
@Setter
@Slf4j
public class MyPriorityQueueOpSize
        extends Operation
        implements PartitionAwareOperation {

    // Input
    private String name;
    // Output
    private Integer response;

    /**
     * Run the {@code size()} operation on the named
     * queue stored in the service.
     */
    @Override
    public void run() {
        log.trace("run() for '{}'", this.name);

        MyPriorityQueueService myPriorityQueueService = super.getService();

        this.response = myPriorityQueueService.getQueue(this.name).size();
    }

    /**
     * Serialize the operation fields to send from local to remote.
     */
    @Override
    protected void writeInternal(ObjectDataOutput objectDataOutput) throws IOException {
        super.writeInternal(objectDataOutput);
        objectDataOutput.writeUTF(this.name);
    }

    /**
     * De-serialize the operation fields to receive from remote to local.
     */
    @Override
    protected void readInternal(ObjectDataInput objectDataInput) throws IOException {
        super.readInternal(objectDataInput);
        this.name = objectDataInput.readUTF();
    }
}
