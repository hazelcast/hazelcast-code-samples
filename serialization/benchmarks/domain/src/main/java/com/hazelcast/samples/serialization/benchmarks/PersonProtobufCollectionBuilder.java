package com.hazelcast.samples.serialization.benchmarks;

import java.util.ArrayList;

import com.hazelcast.samples.serialization.benchmarks.PersonProtobuf.Builder;

/**
 * <p>Converts raw data into "{@code com.hazelcast.samples.serialization.benchmarks.PersonProtobuf}".
 * </p>
 */
public class PersonProtobufCollectionBuilder implements PersonCollectionBuilder {

    private ArrayList<Object> data = new ArrayList<>();

    @Override
    public PersonCollectionBuilder addData(Object[][] raw) {
        PassportProtobuf passport =
                PassportProtobuf.newBuilder()
                .setExpiryDate(MyConstants.EXPIRY_DATE)
                .setIssuingCountry(MyConstants.ISSUING_COUNTRY)
                .setIssuingDate(MyConstants.ISSUING_DATE)
                .build();

        for (Object[] trio : raw) {
            String firstName = (String) trio[0];
            String lastName = (String) trio[1];
            boolean hasPassport = (boolean) trio[2];

            Builder personBuilder = PersonProtobuf.newBuilder();
            personBuilder.setFirstName(firstName);
            personBuilder.setLastName(lastName);
            if (hasPassport) {
                personBuilder.setPassport(passport);
            }
            PersonProtobuf person = personBuilder.build();

            data.add(person);
        }

        return this;
    }

    @Override
    public PersonCollection build() {
        return new PersonCollection(this.data, MyConstants.Kind.PROTOBUF);
    }
}
