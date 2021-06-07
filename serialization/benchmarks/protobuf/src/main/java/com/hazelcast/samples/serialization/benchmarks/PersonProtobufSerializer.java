package com.hazelcast.samples.serialization.benchmarks;

import java.io.IOException;

import com.hazelcast.nio.serialization.ByteArraySerializer;

public class PersonProtobufSerializer implements ByteArraySerializer<PersonProtobuf> {

    @Override
    public int getTypeId() {
        return MyConstants.PERSON_PROTOBUF_SERIALIZER;
    }

    @Override
    public byte[] write(PersonProtobuf object) throws IOException {
        return object.toByteArray();
    }

    @Override
    public PersonProtobuf read(byte[] buffer) throws IOException {
        return PersonProtobuf.parseFrom(buffer);
    }

}
