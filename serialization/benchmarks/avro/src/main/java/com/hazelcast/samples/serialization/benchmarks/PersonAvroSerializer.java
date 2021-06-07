package com.hazelcast.samples.serialization.benchmarks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class PersonAvroSerializer implements StreamSerializer<PersonAvro> {

    private BinaryDecoder binaryDecoder;
    private BinaryEncoder binaryEncoder;
    private DatumReader<PersonAvro> personDataReader;
    private DatumWriter<PersonAvro> personDatumWriter;

    public PersonAvroSerializer() {
        this.personDataReader = new SpecificDatumReader<PersonAvro>(PersonAvro.class);
        this.personDatumWriter = new SpecificDatumWriter<PersonAvro>(PersonAvro.class);
    }

    @Override
    public int getTypeId() {
        return MyConstants.PERSON_AVRO_SERIALIZER;
    }

    /* Using "null" as second arg to obtain new BinaryEncoder seems to have significant
     * performance cost, so better to reuse existing.
     */
    @Override
    public void write(ObjectDataOutput out, PersonAvro object) throws IOException {
        this.binaryEncoder = new EncoderFactory().binaryEncoder((OutputStream) out, this.binaryEncoder);
        this.personDatumWriter.write(object, binaryEncoder);
        binaryEncoder.flush();
    }

    /* Using "null" as second arg to obtain new BinaryDecoder has negligable
     * performance cost compared to reusing existing.
     */
    @Override
    public PersonAvro read(ObjectDataInput in) throws IOException {
        this.binaryDecoder = new DecoderFactory().binaryDecoder((InputStream) in, this.binaryDecoder);
        PersonAvro personAvro = new PersonAvro();
        this.personDataReader.read(personAvro, this.binaryDecoder);
        return personAvro;
    }

}
