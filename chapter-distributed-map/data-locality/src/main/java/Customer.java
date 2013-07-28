import java.io.Serializable;

public class Customer implements Serializable {
    public final long id;

    public Customer(long id) {
        this.id = id;
    }
}