import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;

public class PersonStreamSerializer implements StreamSerializer<Person> {

    @Override
    public void destroy() {
    }

    @Override
    public int getTypeId() {
        return 1;
    }

    @Override
    public void write(ObjectDataOutput out, Person object) throws IOException {
        out.writeUTF(object.getName());
    }

    @Override
    public Person read(ObjectDataInput in) throws IOException {
        return new Person(in.readUTF());
    }
}
