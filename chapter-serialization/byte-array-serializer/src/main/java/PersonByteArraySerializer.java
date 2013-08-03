import com.hazelcast.nio.serialization.ByteArraySerializer;

import java.io.IOException;

public class PersonByteArraySerializer implements ByteArraySerializer<Person> {

    @Override
    public void destroy() {
    }

    @Override
    public int getTypeId() {
        return 1;
    }

    @Override
    public byte[] write(Person object) throws IOException {
        return object.getName().getBytes();
    }

    @Override
    public Person read(byte[] buffer) throws IOException {
        String name = new String(buffer);
        return new Person(name);
    }
}
