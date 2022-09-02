package com.hazelcast.samples.serialization.benchmarks;

/**
 * <p>
 * A domain object that contains no Serialization specific implementation.  For use
 * with new Compact Serialization.
 * </p>
 */
public class PersonCompact {

    private String firstName;
    private String lastName;
    private PassportCompact passport;

    // Java getters, setters and toString.

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public PassportCompact getPassport() {
        return passport;
    }
    public void setPassport(PassportCompact passport) {
        this.passport = passport;
    }
    @Override
    public String toString() {
        return "PersonCompact [firstName=" + firstName + ", lastName=" + lastName + ", passport=" + passport + "]";
    }

}
