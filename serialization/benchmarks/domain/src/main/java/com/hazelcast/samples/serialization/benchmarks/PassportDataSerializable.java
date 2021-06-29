package com.hazelcast.samples.serialization.benchmarks;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

/**
 * <p>
 * A {@link com.hazelcast.nio.serialization.DataSerializable DataSerializable}
 * object, which can be used with languages other than Java.
 * </p>
 */
public class PassportDataSerializable implements com.hazelcast.nio.serialization.DataSerializable {

    private String expiryDate;
    private String issuingCountry;
    private long issuingDate;

    // Hazelcast serialization

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeString(this.expiryDate);
        out.writeString(this.issuingCountry);
        out.writeLong(this.issuingDate);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        this.expiryDate = in.readString();
        this.issuingCountry = in.readString();
        this.issuingDate = in.readLong();
    }

    // Java getters, setters and toString.

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getIssuingCountry() {
        return issuingCountry;
    }

    public void setIssuingCountry(String issuingCountry) {
        this.issuingCountry = issuingCountry;
    }

    public long getIssuingDate() {
        return issuingDate;
    }

    public void setIssuingDate(long issuingDate) {
        this.issuingDate = issuingDate;
    }

    @Override
    public String toString() {
        return "PassportDataSerializable [expiryDate=" + expiryDate + ", issuingCountry=" + issuingCountry
                + ", issuingDate=" + issuingDate + "]";
    }

}
