import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

@SuppressWarnings("unused")
public class Person implements DataSerializable {

    private String name;

    public Person() {
    }

    Person(String name) {
        this.name = name;
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        this.name = in.readUTF();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(name);
    }

    @Override
    public String toString() {
        return String.format("Person(name=%s)", name);
    }
}
