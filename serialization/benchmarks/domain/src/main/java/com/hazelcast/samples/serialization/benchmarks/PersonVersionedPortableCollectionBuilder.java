package com.hazelcast.samples.serialization.benchmarks;

import java.util.ArrayList;

/**
 * <p>Converts raw data into "{@code com.hazelcast.nio.serialization.VersionedPortable}".
 * </p>
 */
public class PersonVersionedPortableCollectionBuilder implements PersonCollectionBuilder {

    private ArrayList<Object> data = new ArrayList<>();

    @Override
    public PersonCollectionBuilder addData(Object[][] raw) {
        PassportVersionedPortable passport = new PassportVersionedPortable();
        passport.setExpiryDate(MyConstants.EXPIRY_DATE);
        passport.setIssuingCountry(MyConstants.ISSUING_COUNTRY);
        passport.setIssuingDate(MyConstants.ISSUING_DATE);

        for (Object[] trio : raw) {
            String firstName = (String) trio[0];
            String lastName = (String) trio[1];
            boolean hasPassport = (boolean) trio[2];

            PersonVersionedPortable person = new PersonVersionedPortable();
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
        return new PersonCollection(this.data, MyConstants.Kind.HAZELCAST_VERSIONED_PORTABLE);
    }
}
