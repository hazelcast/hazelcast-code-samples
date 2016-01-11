import java.io.Serializable;

public class Person implements Serializable {

    private final Name name;

    Person(Name name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "'}";
    }
}
