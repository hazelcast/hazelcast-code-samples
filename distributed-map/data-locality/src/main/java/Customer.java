import java.io.Serializable;

public class Customer implements Serializable {

    private final long id;

    Customer(long id) {
        this.id = id;
    }

    long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Customer{"
                + "id=" + id
                + '}';
    }
}
