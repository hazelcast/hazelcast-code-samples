import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import java.io.IOException;

public class Person implements Portable {

    private String name;

    Person() {
    }

    Person(String name) {
        this.name = name;
    }

    @Override
    public int getClassId() {
        return PortableFactoryImpl.PERSON_CLASS_ID;
    }

    @Override
    public int getFactoryId() {
        return PortableFactoryImpl.FACTORY_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        System.out.println("Serialize");
        writer.writeUTF("name", name);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        System.out.println("Deserialize");
        this.name = reader.readUTF("name");
    }

    @Override
    public String toString() {
        return String.format("Person(name=%s)", name);
    }
}
