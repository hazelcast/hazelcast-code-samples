import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import hazelcast.avro.Person;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PersonAvroSerializer implements StreamSerializer<Person> {
    @Override
    public void write(ObjectDataOutput out, Person object) throws IOException {
        final DatumWriter<Person> personDatumWriter = new SpecificDatumWriter<Person>(Person.class);
        final BinaryEncoder binaryEncoder = new EncoderFactory().binaryEncoder((OutputStream) out, null);
        personDatumWriter.write(object, binaryEncoder);
        binaryEncoder.flush();

    }

    @Override
    public Person read(ObjectDataInput in) throws IOException {
        final DatumReader<Person> reader = new SpecificDatumReader<Person>(Person.class);
        final BinaryDecoder binaryDecoder = new DecoderFactory().binaryDecoder((InputStream) in, null);
        final Person person = new Person();
        reader.read(person, binaryDecoder);
        return person;
    }

    @Override
    public int getTypeId() {
        return 42;
    }

    @Override
    public void destroy() {

    }
}
