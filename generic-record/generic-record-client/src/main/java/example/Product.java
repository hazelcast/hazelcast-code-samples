package example;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import java.io.IOException;

public class Product implements Portable {

    private String name;
    private int quantity;

    public Product(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public Product() {
    }

    @Override
    public int getFactoryId() {
        return 1;
    }

    @Override
    public int getClassId() {
        return 1;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeString("name", name);
        writer.writeInt("quantity", quantity);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        name = reader.readString("name");
        quantity = reader.readInt("quantity");
    }

    @Override
    public String toString() {
        return "example.Product{"
                + "name='" + name + '\''
                + ", quantity=" + quantity
                + '}';
    }
}
