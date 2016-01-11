import java.io.Serializable;

public class Employee implements Serializable {

    private int salary;

    public Employee(int salary) {
        this.salary = salary;
    }

    public int getSalary() {
        return salary;
    }

    public void incSalary(int delta) {
        salary += delta;
    }
}
