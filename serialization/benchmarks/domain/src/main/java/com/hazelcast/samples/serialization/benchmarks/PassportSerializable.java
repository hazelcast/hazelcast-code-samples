package com.hazelcast.samples.serialization.benchmarks;

/**
 * <p>
 * A {@link java.io.Serializable Serializable} object, which can be used with
 * languages other than Java.
 * </p>
 */
public class PassportSerializable implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private String expiryDate;
    private String issuingCountry;
    private long issuingDate;
    
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
		return "PassportSerializable [expiryDate=" + expiryDate + ", issuingCountry=" + issuingCountry
				+ ", issuingDate=" + issuingDate + "]";
	}

}
