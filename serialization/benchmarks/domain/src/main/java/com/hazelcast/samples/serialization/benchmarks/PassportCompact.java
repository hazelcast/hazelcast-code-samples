package com.hazelcast.samples.serialization.benchmarks;

/**
 * <p>
 * A domain object that contains no Serialization specific implementation.  For use
 * with new Compact Serialization.
 * </p>
 */
public class PassportCompact {

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
        return "PassportCompact [expiryDate=" + expiryDate + ", issuingCountry=" + issuingCountry
                + ", issuingDate=" + issuingDate + "]";
    }

}
