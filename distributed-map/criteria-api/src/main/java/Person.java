import java.io.Serializable;

@SuppressWarnings("unused")
public class Person implements Serializable {

    private final String name;
    private final boolean male;
    private final int age;

    public Person(String name, boolean active, int age) {
        this.male = active;
        this.age = age;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isMale() {
        return male;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "Person{"
                + "male=" + male
                + ", name='" + name + '\''
                + ", age=" + age
                + '}';
    }
}
