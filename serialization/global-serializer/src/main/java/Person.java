@SuppressWarnings("unused")
public class Person {

    private String name;

    public Person() {
    }

    Person(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Person{"
                + "name='" + name + '\''
                + '}';
    }
}
