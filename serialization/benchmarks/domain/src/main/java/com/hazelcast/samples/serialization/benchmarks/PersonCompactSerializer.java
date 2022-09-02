package com.hazelcast.samples.serialization.benchmarks;

import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;

public class PersonCompactSerializer implements CompactSerializer<PersonCompact> {
    @Override
    public PersonCompact read(CompactReader compactReader) {
        PersonCompact person = new PersonCompact();
        person.setFirstName(compactReader.readString("firstName"));
        person.setLastName(compactReader.readString("lastName"));
        PassportCompact passport = compactReader.readCompact("passport");
        person.setPassport(passport);
        return person;
    }

    @Override
    public void write(CompactWriter compactWriter, PersonCompact personCompact) {
        compactWriter.writeString("firstName", personCompact.getFirstName());
        compactWriter.writeString("lastName", personCompact.getLastName());
        compactWriter.writeCompact("passport", personCompact.getPassport());
    }

    @Override
    public String getTypeName() {
        return "com.hazelcast.samples.serialization.benchmarks.PersonCompact";
    }

    @Override
    public Class<PersonCompact> getCompactClass() {
        return PersonCompact.class;
    }
}
