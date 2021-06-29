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
public class PersonVersionedPortable implements com.hazelcast.nio.serialization.VersionedPortable {

    private String firstName;
    private String lastName;
    private PassportVersionedPortable passport;

    // Hazelcast serialization

    @Override
    public int getFactoryId() {
        return MyConstants.MY_VERSIONED_PORTABLE_FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return MyConstants.PERSON_VERSIONED_PORTABLE_ID;
    }

    @Override
    public int getClassVersion() {
        return MyConstants.PERSON_VERSIONED_PORTABLE_CLASS_VERSION;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeString("firstName", this.firstName);
        writer.writeString("lastName", this.lastName);
        if (this.passport == null) {
            writer.writeNullPortable("passport",
                    MyConstants.MY_VERSIONED_PORTABLE_FACTORY_ID, MyConstants.PASSPORT_VERSIONED_PORTABLE_ID);
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

    public PassportVersionedPortable getPassport() {
        return passport;
    }

    public void setPassport(PassportVersionedPortable passport) {
        this.passport = passport;
    }

    @Override
    public String toString() {
        return "PersonVersionedPortable [firstName=" + firstName + ", lastName=" + lastName + ", passport=" + passport
                + "]";
    }

}
