public class Car {
    private String color;
    private Person owner;

    public Car(Person owner, String color) {
        this.color = color;
        this.owner = owner;
    }

    public String getColor() {
        return color;
    }

    public Person getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return "Car{" +
                "color='" + color + '\'' +
                ", owner=" + owner +
                '}';
    }
}
