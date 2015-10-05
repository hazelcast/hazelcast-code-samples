import java.io.Serializable;

public class Person implements Serializable {
    public Name name;

    public Person(Name name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "'}";
    }
}