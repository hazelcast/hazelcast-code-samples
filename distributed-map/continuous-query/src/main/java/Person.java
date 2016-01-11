import java.io.Serializable;

public class Person implements Serializable {

    private final String name;

    public Person(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person{"
                + "name='" + name + '\''
                + '}';
    }
}
