package com.hazelcast.samples.nearcache.frauddetection;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.Data;

import java.io.IOException;

/**
 * The domain model for the User, where here we
 * are mostly concerned to record the last place
 * they performed a transaction.
 * <p>
 * Unlike {link Airport}, we use {@link DataSerializable}
 * as the mechanism to retrieve the user from the Hazelcast
 * grid. We don't use a Near Cache for the {@code User} to
 * ensure we use the most up to date copy, so the {@code User}
 * object will transfer from process to process frequently.
 * Hence we want a more efficient serialization strategy than
 * the Java default.
 */
@Data
public class User implements DataSerializable {

    private int userId;
    private String lastCardUsePlace;
    private long lastCardUseTimestamp;

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeInt(this.userId);
        objectDataOutput.writeUTF(this.lastCardUsePlace);
        objectDataOutput.writeLong(this.lastCardUseTimestamp);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        this.userId = objectDataInput.readInt();
        this.lastCardUsePlace = objectDataInput.readUTF();
        this.lastCardUseTimestamp = objectDataInput.readLong();
    }
}
