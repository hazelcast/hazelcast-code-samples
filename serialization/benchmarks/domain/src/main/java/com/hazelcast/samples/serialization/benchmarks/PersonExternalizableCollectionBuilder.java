package com.hazelcast.samples.serialization.benchmarks;

import java.util.ArrayList;

/**
 * <p>Converts raw data into "{@code java.io.Externalizable}".
 * </p>
 */
public class PersonExternalizableCollectionBuilder implements PersonCollectionBuilder {

    private ArrayList<Object> data = new ArrayList<>();

    @Override
    public PersonCollectionBuilder addData(Object[][] raw) {
        PassportExternalizable passport = new PassportExternalizable();
        passport.setExpiryDate(MyConstants.EXPIRY_DATE);
        passport.setIssuingCountry(MyConstants.ISSUING_COUNTRY);
        passport.setIssuingDate(MyConstants.ISSUING_DATE);

        for (Object[] trio : raw) {
            String firstName = (String) trio[0];
            String lastName = (String) trio[1];
            boolean hasPassport = (boolean) trio[2];

            PersonExternalizable person = new PersonExternalizable();
            person.setFirstName(firstName);
            person.setLastName(lastName);
            if (hasPassport) {
                person.setPassport(passport);
            }

            data.add(person);
        }

        return this;
    }

    @Override
    public PersonCollection build() {
        return new PersonCollection(this.data, MyConstants.Kind.JAVA_EXTERNALIZABLE);
    }
}
