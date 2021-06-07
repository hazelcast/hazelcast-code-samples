package com.hazelcast.samples.serialization.benchmarks;

/**
 * <p>
 * A standard Java bean, to use with Kryo.
 * </p>
 */
public class PersonKryo {

    private String firstName;
    private String lastName;
    private PassportKryo passport;
    
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
	public PassportKryo getPassport() {
		return passport;
	}
	public void setPassport(PassportKryo passport) {
		this.passport = passport;
	}
	@Override
	public String toString() {
		return "PersonKryo [firstName=" + firstName + ", lastName=" + lastName + ", passport=" + passport + "]";
	}

}
