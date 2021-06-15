package com.hazelcast.samples.serialization.benchmarks;

import java.io.IOException;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

/**
 * <p>
 * A {@link com.hazelcast.nio.serialization.Portable Portable} object, which can
 * be used with languages other than Java.
 * </p>
 */
public class PassportPortable implements com.hazelcast.nio.serialization.Portable {

    private String expiryDate;
    private String issuingCountry;
    private long issuingDate;

    // Hazelcast serialization

    @Override
    public int getFactoryId() {
        return MyConstants.MY_PORTABLE_FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return MyConstants.PASSPORT_PORTABLE_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeString("expiryDate", this.expiryDate);
        writer.writeString("issuingCountry", this.issuingCountry);
        writer.writeLong("issuingDate", this.issuingDate);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.expiryDate = reader.readString("expiryDate");
        this.issuingCountry = reader.readString("issuingCountry");
        this.issuingDate = reader.readLong("issuingDate");
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
		return "PassportPortable [expiryDate=" + expiryDate + ", issuingCountry=" + issuingCountry + ", issuingDate="
				+ issuingDate + "]";
	}

}
