public class Car {

    private String color;
    private Person owner;

    public Car(Person owner, String color) {
        this.color = color;
        this.owner = owner;
    }

    String getColor() {
        return color;
    }

    Person getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return "Car{"
                + "color='" + color + '\''
                + ", owner=" + owner
                + '}';
    }
}
