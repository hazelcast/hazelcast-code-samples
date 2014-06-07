import java.io.Serializable;

public class Person implements Serializable {
    public String name;
    public boolean male;
    public int age;

    public Person(String name, boolean active, int age) {
        this.male = active;
        this.age = age;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person{" +
                "male=" + male +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}