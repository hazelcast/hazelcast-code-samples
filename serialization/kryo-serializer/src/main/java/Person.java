@SuppressWarnings("unused")
public class Person {

    private String name;

    private Person() {
    }

    public Person(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Person(name=%s)", name);
    }
}
