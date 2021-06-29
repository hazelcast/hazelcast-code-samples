package com.hazelcast.samples.serialization.benchmarks;

/**
 * <p>
 * A standard Java bean, to use with Kryo.
 * </p>
 */
public class PassportKryo {

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
        return "PassportKryo [expiryDate=" + expiryDate + ", issuingCountry=" + issuingCountry + ", issuingDate="
                + issuingDate + "]";
    }

}
