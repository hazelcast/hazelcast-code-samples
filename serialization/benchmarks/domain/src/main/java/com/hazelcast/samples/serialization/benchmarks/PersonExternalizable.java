package com.hazelcast.samples.serialization.benchmarks;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * <p>
 * A {@link java.io.Externalizable Externalizable} object, which can be used
 * with languages other than Java.
 * </p>
 */
public class PersonExternalizable implements java.io.Externalizable {

    private String firstName;
    private String lastName;
    private PassportExternalizable passport;

    // Java Externalizable serialization

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(this.firstName);
        out.writeUTF(this.lastName);
        out.writeObject(this.passport);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.firstName = in.readUTF();
        this.lastName = in.readUTF();
        this.passport = (PassportExternalizable) in.readObject();
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

    public PassportExternalizable getPassport() {
        return passport;
    }

    public void setPassport(PassportExternalizable passport) {
        this.passport = passport;
    }

    @Override
    public String toString() {
        return "PersonExternalizable [firstName=" + firstName + ", lastName=" + lastName + ", passport=" + passport
                + "]";
    }

}
