package com.hazelcast.samples.serialization.benchmarks;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

/**
 * <p>
 * A {@link com.hazelcast.nio.serialization.DataSerializable DataSerializable}
 * object, which can be used with languages other than Java.
 * </p>
 */
public class PersonDataSerializable implements com.hazelcast.nio.serialization.DataSerializable {

    private String firstName;
    private String lastName;
    private PassportDataSerializable passport;

    // Hazelcast serialization

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeString(this.firstName);
        out.writeString(this.lastName);
        out.writeObject(this.passport);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.passport = in.readObject();
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

    public PassportDataSerializable getPassport() {
        return passport;
    }

    public void setPassport(PassportDataSerializable passport) {
        this.passport = passport;
    }

    @Override
    public String toString() {
        return "PersonDataSerializable [firstName=" + firstName + ", lastName=" + lastName + ", passport=" + passport
                + "]";
    }

}
