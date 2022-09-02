package com.hazelcast.samples.serialization.benchmarks;

import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;

public class PassportCompactSerializer implements CompactSerializer<PassportCompact> {
    @Override
    public PassportCompact read(CompactReader compactReader) {
        PassportCompact passport = new PassportCompact();
        passport.setExpiryDate(compactReader.readString("expiryDate"));
        passport.setIssuingCountry(compactReader.readString("issuingCountry"));
        passport.setIssuingDate(compactReader.readInt64("issuingDate"));
        return passport;
    }

    @Override
    public void write(CompactWriter compactWriter, PassportCompact passportCompact) {
        compactWriter.writeString("expiryDate", passportCompact.getExpiryDate());
        compactWriter.writeString("issuingCountry", passportCompact.getIssuingCountry());
        compactWriter.writeInt64("issuingDate", passportCompact.getIssuingDate());
    }

    @Override
    public String getTypeName() {
        return "com.hazelcast.samples.serialization.benchmarks.PassportCompact";
    }

    @Override
    public Class<PassportCompact> getCompactClass() {
        return PassportCompact.class;
    }
}
