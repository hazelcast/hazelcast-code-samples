package your.company.name;

import java.io.Serializable;

/**
 * <p>The domain object, a simple Java bean.
 * </p>
 */
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    private String firstName;
    private int satisfaction;

    // Code below is generated
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public int getSatisfaction() {
        return satisfaction;
    }
    public void setSatisfaction(int satisfaction) {
        this.satisfaction = satisfaction;
    }
    @Override
    public String toString() {
        return "Customer [firstName=" + firstName + ", satisfaction=" + satisfaction + "]";
    }

}
