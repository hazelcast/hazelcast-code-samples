package com.hazelcast.samples.serialization.benchmarks;

import java.util.ArrayList;

/**
 * <p>Converts raw data into "{@code com.hazelcast.nio.serialization.DataSerializable}".
 * </p>
 */
public class PersonDataSerializableCollectionBuilder implements PersonCollectionBuilder {

    private ArrayList<Object> data = new ArrayList<>();

    @Override
    public PersonCollectionBuilder addData(Object[][] raw) {
        PassportDataSerializable passport = new PassportDataSerializable();
        passport.setExpiryDate(MyConstants.EXPIRY_DATE);
        passport.setIssuingCountry(MyConstants.ISSUING_COUNTRY);
        passport.setIssuingDate(MyConstants.ISSUING_DATE);

        for (Object[] trio : raw) {
            String firstName = (String) trio[0];
            String lastName = (String) trio[1];
            boolean hasPassport = (boolean) trio[2];

            PersonDataSerializable person = new PersonDataSerializable();
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
        return new PersonCollection(this.data, MyConstants.Kind.HAZELCAST_DATA_SERIALIZABLE);
    }
}
