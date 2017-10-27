package com.hazelcast.samples.spi;

import java.io.IOException;
import java.util.UUID;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import lombok.Data;

/**
 * <p>An order in this example has an identified and a date
 * when it is due for delivery. Realistically it would have
 * more fields than this.</p>
 * <p>Orders are comparable so we can do them in the order
 * they need to be delivered rather than the order in which
 * they were created.
 * </p>
 * <p>Note the ordering implementation is flawed. An order
 * created on Thursday for Monday delivery will appear before
 * an order created on Thursday for Friday delivery. Only
 * the day is used not the week, and Monday comes before
 * Friday in the collating sequence.</p>
 * <p>It's just an example! You can enhance to fix if you like.</p>
 */
@Data
public class Order implements Comparable<Order>, DataSerializable {

    private UUID id;
    private int seqNo;
    private Day dueDate;

    /**
     * <p>Sort only by day of the week</p>
     */
    @Override
    public int compareTo(Order that) {
        return this.dueDate.compareTo(that.getDueDate());
    }

    /**
     * <p>Send.
     * </p>
     */
    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeObject(this.id);
        objectDataOutput.writeInt(this.seqNo);
        objectDataOutput.writeObject(this.dueDate);
    }
    /**
     * <p>Receive.
     * </p>
     */
    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
            this.id = objectDataInput.readObject();
            this.seqNo = objectDataInput.readInt();
            this.dueDate = objectDataInput.readObject();
    }

}
