import java.io.Serializable;

public class Name implements Serializable {

    private String forename;
    private String surname;

    Name(String forename, String surname) {
        this.forename = forename;
        this.surname = surname;
    }

    @Override
    public String toString() {
        return "Name{"
                + "forename='" + forename + '\''
                + ", surname='" + surname + '\''
                + '}';
    }
}
