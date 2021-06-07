package com.hazelcast.samples.serialization.benchmarks;

import java.util.ArrayList;

/**
 * <p>Converts raw data into "{@code java.io.Serializable}".
 * </p>
 */
public class PersonSerializableCollectionBuilder implements PersonCollectionBuilder {

    private ArrayList<Object> data = new ArrayList<>();

    @Override
    public PersonCollectionBuilder addData(Object[][] raw) {
        PassportSerializable passport = new PassportSerializable();
        passport.setExpiryDate(MyConstants.EXPIRY_DATE);
        passport.setIssuingCountry(MyConstants.ISSUING_COUNTRY);
        passport.setIssuingDate(MyConstants.ISSUING_DATE);

        for (Object[] trio : raw) {
            String firstName = (String) trio[0];
            String lastName = (String) trio[1];
            boolean hasPassport = (boolean) trio[2];

            PersonSerializable person = new PersonSerializable();
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
        return new PersonCollection(this.data, MyConstants.Kind.JAVA_SERIALIZABLE);
    }
}
