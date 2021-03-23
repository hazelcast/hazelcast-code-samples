import java.io.Serializable;
import lombok.Data;

@Data
public class Employee implements Serializable {

    private String firstName;
    private String lastName;
    private int salaryPerMonth;
    private String companyName;
    private int eventId;
    private String categoryName;

    public Employee() {
    }

	/*
	 * The following is provided by Lombok @Data
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getSalaryPerMonth() {
        return salaryPerMonth;
    }

    public void setSalaryPerMonth(int salaryPerMonth) {
        this.salaryPerMonth = salaryPerMonth;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String toString() {
        return companyName + " - " + firstName + " " + lastName + ": " + salaryPerMonth;
    }
	*/
}
