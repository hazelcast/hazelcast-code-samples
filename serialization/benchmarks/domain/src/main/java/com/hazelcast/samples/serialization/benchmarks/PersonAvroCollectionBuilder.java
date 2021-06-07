package com.hazelcast.samples.serialization.benchmarks;

import java.util.ArrayList;

import com.hazelcast.samples.serialization.benchmarks.PersonAvro.Builder;

/**
 * <p>Converts raw data into "{@code com.hazelcast.samples.serialization.benchmarks.PersonAvro}".
 * </p>
 */
public class PersonAvroCollectionBuilder implements PersonCollectionBuilder {

    private ArrayList<Object> data = new ArrayList<>();

    @Override
    public PersonCollectionBuilder addData(Object[][] raw) {
        PassportAvro passport = new PassportAvro();
        passport.setExpiryDate(MyConstants.EXPIRY_DATE);
        passport.setIssuingCountry(MyConstants.ISSUING_COUNTRY);
        passport.setIssuingDate(MyConstants.ISSUING_DATE);

        for (Object[] trio : raw) {
            String firstName = (String) trio[0];
            String lastName = (String) trio[1];
            boolean hasPassport = (boolean) trio[2];

            Builder personBuilder = PersonAvro.newBuilder();
            personBuilder.setFirstName(firstName);
            personBuilder.setLastName(lastName);
            if (hasPassport) {
                personBuilder.setPassport(passport);
            }
            PersonAvro person = personBuilder.build();

            data.add(person);
        }

        return this;
    }

    @Override
    public PersonCollection build() {
        return new PersonCollection(this.data, MyConstants.Kind.AVRO);
    }
}
