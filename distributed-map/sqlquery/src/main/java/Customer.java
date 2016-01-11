import java.io.Serializable;
import java.util.UUID;

public class Customer implements Serializable {

    private final String id = UUID.randomUUID().toString();

    private String name;
    private boolean active;
    private int age;

    Customer(String name, boolean active, int age) {
        this.active = active;
        this.age = age;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Customer{"
                + "active=" + active
                + ", id='" + id + '\''
                + ", name='" + name + '\''
                + ", age=" + age
                + '}';
    }
}
