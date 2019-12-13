import java.io.Serializable;

public class Person implements Serializable {

    private final Name name;

    private final int[] habits;

    public Person(Name name) {
        this.name = name;
        this.habits = null;
    }

    public Person(Name name, int[] habits) {
        this.name = name;
        this.habits = habits;
    }

    public Name getName() {
        return name;
    }

    public int[] getHabits() {
        return habits;
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "'}";
    }

}
