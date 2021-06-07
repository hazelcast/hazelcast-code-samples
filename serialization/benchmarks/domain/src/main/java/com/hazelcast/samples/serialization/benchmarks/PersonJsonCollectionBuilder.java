package com.hazelcast.samples.serialization.benchmarks;

import java.util.ArrayList;

import com.hazelcast.core.HazelcastJsonValue;

/**
 * <p>Converts raw data into "{@code com.hazelcast.core.HazelcastJsonValue}".
 * </p>
 */
public class PersonJsonCollectionBuilder implements PersonCollectionBuilder {

    private ArrayList<Object> data = new ArrayList<>();

    @Override
    public PersonCollectionBuilder addData(Object[][] raw) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append("\"issuingCountry\":\"" + MyConstants.ISSUING_COUNTRY + "\",");
        stringBuilder.append("\"issuingDate\":" + MyConstants.ISSUING_DATE + ",");
        stringBuilder.append("\"expiryDate\":\"" + MyConstants.EXPIRY_DATE + "\"");
        stringBuilder.append("}");

        String passport = stringBuilder.toString();

        for (Object[] trio : raw) {
            String firstName = (String) trio[0];
            String lastName = (String) trio[1];
            boolean hasPassport = (boolean) trio[2];

            stringBuilder = new StringBuilder();

            stringBuilder.append("{");
            stringBuilder.append("\"firstName\":\"" + firstName + "\",");
            stringBuilder.append("\"lastName\":\"" + lastName + "\"");
            if (hasPassport) {
                stringBuilder.append(",\"passport\":" + passport);
            }
            stringBuilder.append("}");

            String person = stringBuilder.toString();

            data.add(new HazelcastJsonValue(person));
        }

        return this;
    }

    @Override
    public PersonCollection build() {
        return new PersonCollection(this.data, MyConstants.Kind.HAZELCAST_JSON_VALUE);
    }
}
