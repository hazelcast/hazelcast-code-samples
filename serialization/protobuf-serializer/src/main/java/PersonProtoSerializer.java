import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import com.hazelcast.serialization.PersonProtos;

import java.io.IOException;

/**
 * Serializer for Person class generated from Protobuf compiler
 *
 * @author Viktor Gamov on 5/12/17.
 *         Twitter: @gamussa
 */
public class PersonProtoSerializer implements StreamSerializer<PersonProtos.Person> {

    @Override
    public void write(ObjectDataOutput out, PersonProtos.Person person) throws IOException {
        out.writeByteArray(person.toByteArray());
    }

    @Override
    public PersonProtos.Person read(ObjectDataInput in) throws IOException {
        return PersonProtos.Person.parseFrom(in.readByteArray());
    }

    @Override
    public int getTypeId() {
        return 42;
    }

    @Override
    public void destroy() {
    }
}
