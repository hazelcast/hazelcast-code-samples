import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;

public class PersonStreamSerializer implements StreamSerializer<Person> {

    @Override
    public int getTypeId() {
        return MySerializationConstants.PERSON_TYPE.getId();
    }

    @Override
    public void write(ObjectDataOutput out, Person object) throws IOException {
        out.writeUTF(object.getName());
    }

    @Override
    public Person read(ObjectDataInput in) throws IOException {
        String name = in.readUTF();
        return new Person(name);
    }

    @Override
    public void destroy() {
    }
}
