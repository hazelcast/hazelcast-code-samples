package com.hazelcast.samples.serialization.benchmarks;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * <p>
 * A {@link java.io.Externalizable Externalizable} object, which can be used
 * with languages other than Java.
 * </p>
 */
public class PassportExternalizable implements java.io.Externalizable {

    private String expiryDate;
    private String issuingCountry;
    private long issuingDate;

    // Java Externalizable serialization

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(this.expiryDate);
        out.writeUTF(this.issuingCountry);
        out.writeLong(this.issuingDate);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.expiryDate = in.readUTF();
        this.issuingCountry = in.readUTF();
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
		return "PassportExternalizable [expiryDate=" + expiryDate + ", issuingCountry=" + issuingCountry
				+ ", issuingDate=" + issuingDate + "]";
	}

}
