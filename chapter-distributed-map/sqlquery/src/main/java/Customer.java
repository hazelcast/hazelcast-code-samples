import java.io.Serializable;
import java.util.UUID;

public class Customer implements Serializable {
    private final String id = UUID.randomUUID().toString();
    public String name;
    public boolean active;
    public int age;

    public Customer(String name, boolean active, int age) {
        this.active = active;
        this.age = age;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "active=" + active +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}