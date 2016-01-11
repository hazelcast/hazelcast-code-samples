import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@SuppressWarnings("unused")
public class Person implements Externalizable {

    private String name;

    public Person() {
    }

    Person(String name) {
        this.name = name;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.name = in.readUTF();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(name);
    }

    @Override
    public String toString() {
        return String.format("Person(name=%s)", name);
    }
}
