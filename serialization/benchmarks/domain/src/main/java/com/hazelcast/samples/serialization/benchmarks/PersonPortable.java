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
public class PersonPortable implements com.hazelcast.nio.serialization.Portable {

    private String firstName;
    private String lastName;
    private PassportPortable passport;

    // Hazelcast serialization

    @Override
    public int getFactoryId() {
        return MyConstants.MY_PORTABLE_FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return MyConstants.PERSON_PORTABLE_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeString("firstName", this.firstName);
        writer.writeString("lastName", this.lastName);
        if (this.passport == null) {
            writer.writeNullPortable("passport", MyConstants.MY_PORTABLE_FACTORY_ID, MyConstants.PASSPORT_PORTABLE_ID);
        } else {
            writer.writePortable("passport", this.passport);
        }
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.firstName = reader.readString("firstName");
        this.lastName = reader.readString("lastName");
        this.passport = reader.readPortable("passport");
    }

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

	public PassportPortable getPassport() {
		return passport;
	}

	public void setPassport(PassportPortable passport) {
		this.passport = passport;
	}

	@Override
	public String toString() {
		return "PersonPortable [firstName=" + firstName + ", lastName=" + lastName + ", passport=" + passport + "]";
	}

}
