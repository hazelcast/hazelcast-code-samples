package com.hazelcast.samples.serialization.benchmarks;

/**
 * <p>
 * A {@link java.io.Serializable Serializable} object, which can be used with
 * languages other than Java.
 * </p>
 */
public class PersonSerializable implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private String firstName;
    private String lastName;
    private PassportSerializable passport;
    
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
	public PassportSerializable getPassport() {
		return passport;
	}
	public void setPassport(PassportSerializable passport) {
		this.passport = passport;
	}
	@Override
	public String toString() {
		return "PersonSerializable [firstName=" + firstName + ", lastName=" + lastName + ", passport=" + passport + "]";
	}

}
